package uk.gov.ons.ctp.response.action.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanRepository;
import uk.gov.ons.ctp.response.action.service.ActionPlanService;

/**
 * Implementation
 */
@Service
@Slf4j
public class ActionPlanServiceImpl implements ActionPlanService {

  private static final int TRANSACTION_TIMEOUT = 30;

  @Autowired
  private ActionPlanRepository actionPlanRepo;

  @Override
  public List<ActionPlan> findActionPlans() {
    log.debug("Entering findActionPlans");
    return actionPlanRepo.findAll();
  }

  @Override
  public ActionPlan findActionPlan(final Integer actionPlanKey) {
    log.debug("Entering findActionPlan with primary key {}", actionPlanKey);
    return actionPlanRepo.findOne(actionPlanKey);
  }

  @Override
  public ActionPlan findActionPlanById(final UUID actionPlanId) {
    log.debug("Entering findActionPlanById with id {}", actionPlanId);
    return actionPlanRepo.findById(actionPlanId);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false, timeout = TRANSACTION_TIMEOUT)
  public ActionPlan updateActionPlan(final UUID actionPlanId, final ActionPlan actionPlan) {
    log.debug("Entering updateActionPlan with id {}", actionPlanId);
    ActionPlan existingActionPlan = actionPlanRepo.findById(actionPlanId);
    if (existingActionPlan != null) {
      boolean needsUpdate = false;

      String newDescription = actionPlan.getDescription();
      log.debug("newDescription = {}", newDescription);
      if (newDescription != null) {
        needsUpdate = true;
        existingActionPlan.setDescription(newDescription);
      }

      Date newLastRunDateTime = actionPlan.getLastRunDateTime();
      log.debug("newLastRunDatetime = {}", newLastRunDateTime);
      if (newLastRunDateTime != null) {
        needsUpdate = true;
        existingActionPlan.setLastRunDateTime(new Timestamp(newLastRunDateTime.getTime()));
      }

      if (needsUpdate) {
        log.debug("about to update the action plan with id {}", actionPlanId);
        existingActionPlan = actionPlanRepo.save(existingActionPlan);
      }
    }
    return existingActionPlan;
  }
}
