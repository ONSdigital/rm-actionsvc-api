package uk.gov.ons.ctp.response.action.endpoint;


import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.domain.model.ActionCase;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.representation.ActionFeedbackDTO;
import uk.gov.ons.ctp.response.action.service.ActionCaseService;
import uk.gov.ons.ctp.response.action.service.ActionPlanService;
import uk.gov.ons.ctp.response.action.service.ActionService;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * The REST endpoint controller for Actions.
 */
@RestController
@RequestMapping(value = "/actions", produces = "application/json")
@Slf4j
public final class ActionEndpoint implements CTPEndpoint {

  @Autowired
  private ActionService actionService;

  @Autowired
  private ActionPlanService actionPlanService;

  @Autowired
  private ActionCaseService actionCaseService;

  @Qualifier("actionBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  public static final String ACTION_NOT_FOUND = "Action not found for id %s";
  public static final String ACTION_NOT_UPDATED = "Action not updated for id %s";

  /**
   * GET the Action for the specified action id.
   *
   * @param actionId Action Id of requested Action
   * @return ActionDTO Returns the associated Action for the specified action
   * @throws CTPException if no associated Action found for the specified action
   *           Id.
   */
  @RequestMapping(value = "/{actionid}", method = RequestMethod.GET)
  public ActionDTO findActionByActionId(@PathVariable("actionid") final UUID actionId) throws CTPException {
    log.info("Entering findActionByActionId with {}", actionId);
    Action action = actionService.findActionById(actionId);
    if (action == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, "Action not found for id %s", actionId);
    }

    ActionDTO actionDTO = mapperFacade.map(action, ActionDTO.class);
    UUID actionPlanUUID = actionPlanService.findActionPlan(action.getActionPlanFK()).getId();
    actionDTO.setActionPlanId(actionPlanUUID);
    return actionDTO;
  }

  /**
   * GET Actions for the specified case Id.
   *
   * @param caseId caseID to which Actions apply
   * @return List<ActionDTO> Returns the associated actions for the specified
   *         case id.
   */
  @RequestMapping(value = "/case/{caseid}", method = RequestMethod.GET)
  public ResponseEntity<?> findActionsByCaseId(@PathVariable("caseid") final UUID caseId) {
    log.info("Entering findActionsByCaseId...");
    List<Action> actions = actionService.findActionsByCaseId(caseId);
    if (CollectionUtils.isEmpty(actions)) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.ok(buildActionsDTOs(actions));
    }
  }

  /**
   * GET all Actions optionally filtered by ActionType and or state
   *
   * @param actionType Optional filter by ActionType
   * @param actionState Optional filter by Action state
   * @return List<ActionDTO> Actions for the specified filters
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<?> findActions(@RequestParam(value = "actiontype", required = false) final String actionType,
                                       @RequestParam(value = "state", required = false) final ActionDTO.ActionState
                                               actionState) {
    List<Action> actions = null;

    if (actionType != null) {
      if (actionState != null) {
        log.info("Entering findActionsByTypeAndState with {} {}", actionType, actionState);
        actions = actionService.findActionsByTypeAndStateOrderedByCreatedDateTimeDescending(actionType, actionState);
      } else {
        log.info("Entering findActionsByType with {}", actionType);
        actions = actionService.findActionsByType(actionType);
      }
    } else {
      if (actionState != null) {
        log.info("Entering findActionsByState with {}", actionState);
        actions = actionService.findActionsByState(actionState);
      } else {
        log.info("Entering findAllActionsOrderedByCreatedDateTimeDescending");
        actions = actionService.findAllActionsOrderedByCreatedDateTimeDescending();
      }
    }

    if (CollectionUtils.isEmpty(actions)) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.ok(buildActionsDTOs(actions));
    }
  }

  /**
   * POST Create an Action.
   *
   * @param actionDTO Incoming ActionDTO with details to validate and from which
   *          to create Action
   * @param bindingResult collects errors thrown by update
   * @return ActionDTO Created Action
   * @throws CTPException on failure to create Action
   * @throws InvalidRequestException if binding errors
   */
  @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<?> createAction(final @RequestBody @Valid ActionDTO actionDTO, BindingResult bindingResult)
          throws CTPException, InvalidRequestException {
    log.info("Entering createAction with Action {}", actionDTO);
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for create action: ", bindingResult);
    }

    Action action = actionService.createAction(mapperFacade.map(actionDTO, Action.class));
    ActionDTO resultDTO = mapperFacade.map(action, ActionDTO.class);
    UUID actionPlanUUID = actionPlanService.findActionPlan(action.getActionPlanFK()).getId();
    resultDTO.setActionPlanId(actionPlanUUID);
    return ResponseEntity.created(URI.create("TODO")).body(resultDTO);
  }

  /**
   * PUT to update the specified Action.
   *
   * @param actionId Action Id of the Action to update
   * @param actionDTO Incoming ActionDTO with details to update
   * @param bindingResult collects errors thrown by update
   * @return ActionDTO Returns the updated Action details
   * @throws CTPException if update operation fails
   * @throws InvalidRequestException if binding errors
   */
  @RequestMapping(value = "/{actionid}", method = RequestMethod.PUT, consumes = "application/json")
  public ActionDTO updateAction(@PathVariable("actionid") final UUID actionId,
                                @RequestBody final ActionDTO actionDTO, BindingResult bindingResult)
          throws CTPException, InvalidRequestException {
    log.info("Updating Action with {} - {}", actionId, actionDTO);
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for update action: ", bindingResult);
    }

    actionDTO.setId(actionId);
    Action action = actionService.updateAction(mapperFacade.map(actionDTO, Action.class));
    if (action == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, ACTION_NOT_UPDATED, actionId);
    }

    ActionDTO resultDTO = mapperFacade.map(action, ActionDTO.class);
    UUID actionPlanUUID = actionPlanService.findActionPlan(action.getActionPlanFK()).getId();
    resultDTO.setActionPlanId(actionPlanUUID);
    return resultDTO;
  }

  /**
   * PUT to cancel all the Actions for a specified caseId.
   *
   * @param caseId Case Id of the actions to cancel
   * @return List<ActionDTO> Returns a list of cancelled Actions
   * @throws CTPException if update operation fails
   */
  @RequestMapping(value = "/case/{caseid}/cancel", method = RequestMethod.PUT, consumes = "application/json")
  public ResponseEntity<?> cancelActions(@PathVariable("caseid") final UUID caseId)
      throws CTPException {
    log.info("Cancelling Actions for {}", caseId);

    ActionCase caze = actionCaseService.findActionCase(caseId);
    if (caze == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, "Case not found for caseId %s", caseId);
    }

    List<Action> actions = actionService.cancelActions(caseId);
    if (CollectionUtils.isEmpty(actions)) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.ok(buildActionsDTOs(actions));
    }
  }

  /**
   * Allow feedback otherwise sent via JMS to be sent via endpoint
   * @param actionId the action
   * @param actionFeedbackDTO the feedback
   * @param bindingResult the bindingResult
   * @return the modified action
   * @throws CTPException oops
   * @throws InvalidRequestException if binding errors
   */
  @RequestMapping(value = "/{actionid}/feedback", method = RequestMethod.PUT, consumes = {"application/json"})
  public ActionDTO feedbackAction(@PathVariable("actionid") final UUID actionId,
                                  @RequestBody final ActionFeedbackDTO actionFeedbackDTO, BindingResult bindingResult)
          throws CTPException, InvalidRequestException {
    log.info("Feedback for Action {} - {}", actionId, actionFeedbackDTO);
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for feedback action: ", bindingResult);
    }

    actionFeedbackDTO.setActionId(actionId.toString());
    Action action = actionService.feedBackAction(mapperFacade.map(actionFeedbackDTO, ActionFeedback.class));
    if (action == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, "Action not found for id %s", actionId);
    }

    ActionDTO resultDTO = mapperFacade.map(action, ActionDTO.class);
    UUID actionPlanUUID = actionPlanService.findActionPlan(action.getActionPlanFK()).getId();
    resultDTO.setActionPlanId(actionPlanUUID);
    return resultDTO;
  }

  /**
   * To build a list of ActionDTOs from Actions populating the actionPlanUUID
   *
   * @param actions a list of Actions
   * @return a list of ActionDTOs
   */
  private List<ActionDTO> buildActionsDTOs(List<Action> actions) {
    List<ActionDTO> actionsDTOs = mapperFacade.mapAsList(actions, ActionDTO.class);

    int index = 0;
    for (Action action : actions) {
      int actionPlanFK = action.getActionPlanFK();
      UUID actionPlanUUID = actionPlanService.findActionPlan(actionPlanFK).getId();
      actionsDTOs.get(index).setActionPlanId(actionPlanUUID);
      index = index + 1;
    }

    return actionsDTOs;
  }
}
