package uk.gov.ons.ctp.response.action.endpoint;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.action.service.ActionPlanService;

/**
 * The REST endpoint controller for ActionPlans.
 */
@RestController
@RequestMapping(value = "/actionplans", produces = "application/json")
@Slf4j
public class ActionPlanEndpoint implements CTPEndpoint {

  public static final String ACTION_PLAN_NOT_FOUND = "ActionPlan not found for id %s";

  @Autowired
  private ActionPlanService actionPlanService;

  @Qualifier("actionBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * This method returns all action plans.
   *
   * @return List<ActionPlanDTO> This returns all action plans.
   */
  @RequestMapping(method = RequestMethod.GET)
  public final ResponseEntity<?> findActionPlans() {
    log.info("Entering findActionPlans...");
    List<ActionPlan> actionPlans = actionPlanService.findActionPlans();
    List<ActionPlanDTO> actionPlanDTOs = mapperFacade.mapAsList(actionPlans, ActionPlanDTO.class);
    return CollectionUtils.isEmpty(actionPlanDTOs) ?
            ResponseEntity.noContent().build() : ResponseEntity.ok(actionPlanDTOs);
  }

  /**
   * This method returns the associated action plan for the specified action plan id.
   *
   * @param actionPlanId This is the action plan id
   * @return ActionPlanDTO This returns the associated action plan for the specified action plan id.
   * @throws CTPException if no action plan found for the specified action plan id.
   */
  @RequestMapping(value = "/{actionplanid}", method = RequestMethod.GET)
  public final ActionPlanDTO findActionPlanByActionPlanId(@PathVariable("actionplanid") final UUID actionPlanId)
      throws CTPException {
    log.info("Entering findActionPlanByActionPlanId with {}", actionPlanId);
    ActionPlan actionPlan = actionPlanService.findActionPlanById(actionPlanId);
    if (actionPlan == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, ACTION_PLAN_NOT_FOUND, actionPlanId);
    }
    return mapperFacade.map(actionPlan, ActionPlanDTO.class);
  }

  /**
   * This method returns the associated action plan after it has been updated. Note that only the description and
   * the lastGoodRunDatetime can be updated.
   *
   * @param actionPlanId This is the action plan id
   * @param requestObject The object created by ActionPlanDTOMessageBodyReader from the json found in the request body
   * @param bindingResult collects errors thrown by update
   * @return ActionPlanDTO This returns the updated action plan.
   * @throws CTPException if the json provided is incorrect or if the action plan id does not exist.
   * @throws InvalidRequestException if binding errors
   */
  @RequestMapping(value = "/{actionplanid}", method = RequestMethod.PUT, consumes = "application/json")
  public final ActionPlanDTO updateActionPlanByActionPlanId(@PathVariable("actionplanid") final UUID actionPlanId,
                                                            @RequestBody final ActionPlanDTO requestObject,
                                                            BindingResult bindingResult)
          throws CTPException, InvalidRequestException {
    log.info("UpdateActionPlanByActionPlanId with actionplanid {} - actionPlan {}", actionPlanId, requestObject);
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for update action plan: ", bindingResult);
    }

    ActionPlan actionPlan = actionPlanService.updateActionPlan(actionPlanId,
            mapperFacade.map(requestObject, ActionPlan.class));
    if (actionPlan == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, ACTION_PLAN_NOT_FOUND, actionPlanId);
    }
    return mapperFacade.map(actionPlan, ActionPlanDTO.class);
  }
}
