package uk.gov.ons.ctp.response.action.endpoint;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlanJob;
import uk.gov.ons.ctp.response.action.representation.ActionPlanJobDTO;
import uk.gov.ons.ctp.response.action.service.ActionPlanJobService;
import uk.gov.ons.ctp.response.action.service.ActionPlanService;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static uk.gov.ons.ctp.response.action.endpoint.ActionPlanEndpoint.ACTION_PLAN_NOT_FOUND;

/**
 * The REST endpoint controller for ActionPlanJobs.
 */
@RestController
@RequestMapping(value = "/actionplans", produces = "application/json")
@Slf4j
public class ActionPlanJobEndpoint implements CTPEndpoint {

  public static final String ACTION_PLAN_JOB_NOT_FOUND = "ActionPlanJob not found for id %s";

  @Autowired
  private ActionPlanJobService actionPlanJobService;

  @Autowired
  private ActionPlanService actionPlanService;

  @Qualifier("actionBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * This method returns the associated action plan job for the specified action plan job id.
   *
   * @param actionPlanJobId This is the action plan job id
   * @return ActionPlanJobDTO This returns the associated action plan job for the specified action plan job id.
   * @throws CTPException if no action plan job found for the specified action plan job id.
   */
  @RequestMapping(value = "/jobs/{actionplanjobid}", method = RequestMethod.GET)
  public final ActionPlanJobDTO findActionPlanJobById(@PathVariable("actionplanjobid") final UUID actionPlanJobId)
      throws CTPException {
    log.info("Entering findActionPlanJobById with {}", actionPlanJobId);
    ActionPlanJob actionPlanJob = actionPlanJobService.findActionPlanJob(actionPlanJobId);

    if (actionPlanJob == null){
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, ACTION_PLAN_JOB_NOT_FOUND, actionPlanJobId);
    }

    ActionPlan actionPlan = actionPlanService.findActionPlan(actionPlanJob.getActionPlanFK());
    ActionPlanJobDTO actionPlanJobDTO = mapperFacade.map(actionPlanJob, ActionPlanJobDTO.class);
    actionPlanJobDTO.setActionPlanId(actionPlan.getId());
    return actionPlanJobDTO;
  }

  /**
   * Returns all action plan jobs for the given action plan id.
   * @param actionPlanId the given action plan id.
   * @return Returns all action plan jobs for the given action plan id.
   * @throws CTPException summats went wrong
   */
  @RequestMapping(value = "/{actionplanid}/jobs", method = RequestMethod.GET)
  public final ResponseEntity<?> findAllActionPlanJobsByActionPlanId(@PathVariable("actionplanid") final
                                                                     UUID actionPlanId) throws CTPException {
    log.info("Entering findAllActionPlanJobsByActionPlanId with {}", actionPlanId);
    List<ActionPlanJob> actionPlanJobs = actionPlanJobService.findActionPlanJobsForActionPlan(actionPlanId);
    if (CollectionUtils.isEmpty(actionPlanJobs)) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.ok(buildActionPlanJobDTOs(actionPlanJobs, actionPlanId));
    }
  }

  /**
   * To create a new Action Plan Job having received an action plan id and some json
   * @param actionPlanId the given action plan id.
   * @param actionPlanJobDTO the ActionPlanJobDTO representation of the provided json
   * @param bindingResult collects errors thrown by update
   * @return the created ActionPlanJobDTO
   * @throws CTPException summats went wrong
   * @throws InvalidRequestException if binding errors
   */
  @RequestMapping(value = "/{actionplanid}/jobs", method = RequestMethod.POST, consumes = "application/json")
  public final ResponseEntity<?> executeActionPlan(@PathVariable("actionplanid") final UUID actionPlanId,
      final @RequestBody @Valid ActionPlanJobDTO actionPlanJobDTO, BindingResult bindingResult)
          throws CTPException, InvalidRequestException {
    log.info("Entering executeActionPlan with {}", actionPlanId);

    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for execute action plan: ", bindingResult);
    }

    ActionPlanJob job = mapperFacade.map(actionPlanJobDTO, ActionPlanJob.class);
    job.setId(actionPlanId);
    job = actionPlanJobService.createAndExecuteActionPlanJob(job);
    if (job == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, ACTION_PLAN_NOT_FOUND, actionPlanId);
    } else {
      ActionPlanJobDTO result = mapperFacade.map(job, ActionPlanJobDTO.class);
      result.setActionPlanId(actionPlanId);
      return ResponseEntity.created(URI.create("TODO")).body(result);
    }
  }

  /**
   * To build a list of ActionPlanJobDTOs from ActionPlanJobs populating the actionPlanUUID
   *
   * @param actionPlanJobs a list of ActionPlanJobs
   * @return a list of ActionPlanJobDTOs
   */
  private List<ActionPlanJobDTO> buildActionPlanJobDTOs(List<ActionPlanJob> actionPlanJobs, UUID actionPlanId) {
    List<ActionPlanJobDTO> actionPlanJobsDTOs = mapperFacade.mapAsList(actionPlanJobs, ActionPlanJobDTO.class);

    for (ActionPlanJobDTO actionPlanJobDTO : actionPlanJobsDTOs) {
      actionPlanJobDTO.setActionPlanId(actionPlanId);
    }

    return actionPlanJobsDTOs;
  }
}
