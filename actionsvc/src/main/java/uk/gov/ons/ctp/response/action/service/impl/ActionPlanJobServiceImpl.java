package uk.gov.ons.ctp.response.action.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.distributed.DistributedLockManager;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlanJob;
import uk.gov.ons.ctp.response.action.domain.repository.ActionCaseRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanJobRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanRepository;
import uk.gov.ons.ctp.response.action.representation.ActionPlanJobDTO;
import uk.gov.ons.ctp.response.action.service.ActionPlanJobService;

import java.sql.Timestamp;
import java.util.*;

/**
 * Implementation
 */
@Service
@Slf4j
public class ActionPlanJobServiceImpl implements ActionPlanJobService {

  private static final String ACTION_PLAN_SPAN = "automatedActionPlanExecution";

  public static final String CREATED_BY_SYSTEM = "SYSTEM";
  public static final String NO_ACTIONPLAN_MSG = "ActionPlan not found for id %s";

  @Autowired
  private DistributedLockManager actionPlanExecutionLockManager;

  @Autowired
  private Tracer tracer;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private ActionPlanRepository actionPlanRepo;

  @Autowired
  private ActionCaseRepository actionCaseRepo;

  @Autowired
  private ActionPlanJobRepository actionPlanJobRepo;

  @Override
  public ActionPlanJob findActionPlanJob(final UUID actionPlanJobId) {
    log.debug("Entering findActionPlanJob with id {}", actionPlanJobId);
    return actionPlanJobRepo.findById(actionPlanJobId);
  }

  @Override
  public List<ActionPlanJob> findActionPlanJobsForActionPlan(final UUID actionPlanId) throws CTPException {
    log.debug("Entering findActionPlanJobsForActionPlan with {}", actionPlanId);
    ActionPlan actionPlan = actionPlanRepo.findById(actionPlanId);
    if (actionPlan == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, NO_ACTIONPLAN_MSG, actionPlanId);
    }

    return actionPlanJobRepo.findByActionPlanFK(actionPlan.getActionPlanPK());
  }

  @Override
  public List<ActionPlanJob> createAndExecuteAllActionPlanJobs() {
    Span span = tracer.createSpan(ACTION_PLAN_SPAN);
    List<ActionPlanJob> executedJobs = new ArrayList<>();
    actionPlanRepo.findAll().forEach(actionPlan -> {
      ActionPlanJob job = new ActionPlanJob();
      job.setActionPlanFK(actionPlan.getActionPlanPK());
      job.setCreatedBy(CREATED_BY_SYSTEM);
      job = createAndExecuteActionPlanJob(job, false);
      if (job != null) {
        executedJobs.add(job);
      }
    });
    tracer.close(span);
    return executedJobs;
  }

  @Override
  public ActionPlanJob createAndExecuteActionPlanJob(final ActionPlanJob actionPlanJob) {
    return createAndExecuteActionPlanJob(actionPlanJob, true);
  }

  /**
   * the root method for executing an action plan - called indirectly by the
   * restful endpoint when executing a single plan manually and by the scheduled
   * execution of all plans in sequence. See the other createAndExecute plan
   * methods in this class
   *
   * @param actionPlanJob the plan to execute
   * @param forcedExecution true when called indirectly for manual execution -
   *          the plan lock is still used (we don't want more than one
   *          concurrent plan execution), but we skip the last run time check
   * @return the plan job if it was run or null if not
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  private ActionPlanJob createAndExecuteActionPlanJob(final ActionPlanJob actionPlanJob, boolean forcedExecution) {
    ActionPlanJob createdJob = null;

    Integer actionPlanPK = actionPlanJob.getActionPlanFK();
    ActionPlan actionPlan = actionPlanRepo.findOne(actionPlanPK);
    if (actionPlan != null) {
      if (actionPlanExecutionLockManager.lock(actionPlan.getName())) {
        try {
          Timestamp now = DateTimeUtil.nowUTC();
          if (!forcedExecution) {
            Date lastExecutionTime = new Date(now.getTime() - appConfig.getPlanExecution().getDelayMilliSeconds());
            if (actionPlan.getLastRunDateTime() != null && actionPlan.getLastRunDateTime().after(lastExecutionTime)) {
              log.debug("Job for plan {} has been run since last wake up - skipping", actionPlanPK);
              return createdJob;
            }
          }

          // if no cases for actionplan why bother?
          if (actionCaseRepo.countByActionPlanFK(actionPlanPK) == 0) {
            log.debug("No open cases for action plan {} - skipping", actionPlanPK);
            return createdJob;
          }

          // enrich and save the job
          actionPlanJob.setState(ActionPlanJobDTO.ActionPlanJobState.SUBMITTED);
          actionPlanJob.setCreatedDateTime(now);
          actionPlanJob.setUpdatedDateTime(now);
          actionPlanJob.setId(UUID.randomUUID());
          createdJob = actionPlanJobRepo.save(actionPlanJob);
          log.info("Running actionplanjobid {} actionplanid {}", createdJob.getActionPlanJobPK(),
                  createdJob.getActionPlanFK());
          // get the repo to call sql function to create actions
          actionCaseRepo.createActions(createdJob.getActionPlanJobPK());
        } finally {
          log.debug("Releasing lock on action plan {}", actionPlanPK);
          actionPlanExecutionLockManager.unlock(actionPlan.getName());
        }
      } else {
        log.debug("Could not get lock on action plan {}", actionPlanPK);
      }
    }

    return createdJob;
  }
}
