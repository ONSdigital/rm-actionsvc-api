package uk.gov.ons.ctp.response.action.scheduled.distribution;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.domain.model.Action.ActionPriority;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.model.ActionType;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionTypeRepository;
import uk.gov.ons.ctp.response.action.message.ActionInstructionPublisher;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;
import uk.gov.ons.ctp.response.action.message.instruction.ActionCancel;
import uk.gov.ons.ctp.response.action.message.instruction.ActionContact;
import uk.gov.ons.ctp.response.action.message.instruction.ActionEvent;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.representation.ActionDTO.ActionState;
import uk.gov.ons.ctp.response.action.service.CaseSvcClientService;
import uk.gov.ons.ctp.response.action.service.CollectionExerciseClientService;
import uk.gov.ons.ctp.response.action.service.PartySvcClientService;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDetailsDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseEventDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CategoryDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

/**
 * This is the 'service' class that distributes actions to downstream services, ie services outside of Response
 * Management (ActionExporterSvc, NotifyGW, etc.).
 *
 * It has a number of injected beans, including a RestClient, Repositories and the ActionInstructionPublisher.
 *
 * This class has a self scheduled method wakeUp(), which looks for Actions in
 * SUBMITTED state to send to downstream handlers. On each wake cycle, it
 * fetches the first n actions of each type, by createddatatime, and attempts to
 * enrich them with case, questionnaire, address and caseevent details all
 * fetched in individual calls to the Case service through its RESTful
 * endpoints.
 *
 * It then updates its own action table to change the action state to PENDING,
 * posts a new CaseEvent to the Case Service, and constructs an outbound
 * ActionRequest instance. That instance is added to the list of request objects
 * that will sent out as a batch inside an ActionInstruction to the
 * SpringIntegration @Publisher once the N actions for the individual type have
 * all been processed.
 *
 */
@Component
@Slf4j
public class ActionDistributor {

  // WILL NOT WORK WITHOUT THIS NEXT LINE
  private static final long IMPOSSIBLE_ACTION_ID = 999999999999L;

  @Autowired
  private DistributedListManager<BigInteger> actionDistributionListManager;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private StateTransitionManager<ActionState, ActionDTO.ActionEvent> actionSvcStateTransitionManager;

  @Autowired
  private ActionInstructionPublisher actionInstructionPublisher;

  @Autowired
  private MapperFacade mapperFacade;

  @Autowired
  private ActionRepository actionRepo;

  @Autowired
  private ActionPlanRepository actionPlanRepo;

  @Autowired
  private CaseSvcClientService caseSvcClientService;

  @Autowired
  private CollectionExerciseClientService collectionExerciseClientService;
  
  @Autowired
  private PartySvcClientService partySvcClientService;

  @Autowired
  private ActionTypeRepository actionTypeRepo;

  /**
   * wake up on schedule and check for submitted actions, enrich and distribute them to spring integration channels
   *
   * @return the info for the health endpoint regarding the distribution just performed
   */
  public final DistributionInfo distribute() {
    log.debug("ActionDistributor awoken...");
    DistributionInfo distInfo = new DistributionInfo();

    try {
      List<ActionType> actionTypes = actionTypeRepo.findAll();

      if (!CollectionUtils.isEmpty(actionTypes)) {
        for (ActionType actionType : actionTypes) {
          log.debug("Dealing with actionType {}", actionType.getName());
          int successesForActionRequests = 0;
          int successesForActionCancels = 0;

          List<Action> actions = null;
          try {
            actions = retrieveActions(actionType);
          } catch (Exception e) {
            log.error("Failed to obtain actions - error msg {} - cause {}", e.getMessage(), e.getCause());
          }

          if (!CollectionUtils.isEmpty(actions)) {
            log.debug("Dealing with actions {}", actions.stream().map(a -> a.getActionPK().toString()).collect(
                Collectors.joining(",")));
            for (Action action : actions) {
              try {
                if (action.getState().equals(ActionDTO.ActionState.SUBMITTED)) {
                  processActionRequest(action);
                  successesForActionRequests++;
                } else if (action.getState().equals(ActionDTO.ActionState.CANCEL_SUBMITTED)) {
                  processActionCancel(action);
                  successesForActionCancels++;
                }
              } catch (Exception e) {
                log.error("Exception {} thrown processing action {}. Processing will be retried at next scheduled "
                        + "distribution", e.getMessage(), action.getId());
              }
            }

            try {
              actionDistributionListManager.deleteList(actionType.getName(), true);
            } catch (LockingException e) {
              log.error("Failed to remove the list of actions just processed from distributed list - actions distributed"
                  + " OK, but underlying problem may remain");
            }
          }

          try {
            actionDistributionListManager.unlockContainer();
          } catch (LockingException e) {
            // oh well - it will unlock soon enough
          }

          distInfo.getInstructionCounts().add(new InstructionCount(actionType.getName(),
              DistributionInfo.Instruction.REQUEST, successesForActionRequests));
          distInfo.getInstructionCounts().add(new InstructionCount(actionType.getName(),
              DistributionInfo.Instruction.CANCEL_REQUEST, successesForActionCancels));
        }
      }
    } catch (Exception e) {
      log.error("Failed to process actions because {}", e.getMessage());
    }

    log.debug("ActionDistributor going back to sleep");
    return distInfo;
  }

  /**
   * Get the oldest page of submitted actions by type - but do not retrieve the
   * same cases as other CaseSvc' in the cluster
   *
   * @param actionType the type
   * @return list of actions
   * @throws LockingException LockingException thrown
   */
  private List<Action> retrieveActions(ActionType actionType) throws LockingException {
    Pageable pageable = new PageRequest(0, appConfig.getActionDistribution().getRetrievalMax(), new Sort(
        new Sort.Order(Direction.ASC, "updatedDateTime")));

    List<BigInteger> excludedActionIds = actionDistributionListManager.findList(actionType.getName(), false);
    if (!excludedActionIds.isEmpty()) {
      log.debug("Excluding actions {}", excludedActionIds);
    }
    // DO NOT REMOVE THIS NEXT LINE
    excludedActionIds.add(BigInteger.valueOf(IMPOSSIBLE_ACTION_ID));

    List<Action> actions = actionRepo
        .findByActionTypeNameAndStateInAndActionPKNotIn(actionType.getName(),
            Arrays.asList(ActionState.SUBMITTED, ActionState.CANCEL_SUBMITTED), excludedActionIds, pageable);
    if (!CollectionUtils.isEmpty(actions)) {
      log.debug("RETRIEVED action ids {}", actions.stream().map(a -> a.getActionPK().toString())
          .collect(Collectors.joining(",")));
      // try and save our list to the distributed store
      actionDistributionListManager.saveList(actionType.getName(), actions.stream()
          .map(action -> action.getActionPK())
          .collect(Collectors.toList()), true);
    }
    return actions;
  }

  /**
   * Deal with a single action - the transaction boundary is here.
   *
   * The processing requires numerous calls to Case service, to write to our own action table and to publish to queue.
   *
   * @param action the action to deal with
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  private void processActionRequest(final Action action) throws CTPException {
    log.debug("processing actionRequest with actionid {} caseid {} actionplanFK {}", action.getId(),
        action.getCaseId(), action.getActionPlanFK());

    ActionDTO.ActionEvent event = action.getActionType().getResponseRequired() ?
        ActionDTO.ActionEvent.REQUEST_DISTRIBUTED : ActionDTO.ActionEvent.REQUEST_COMPLETED;

    transitionAction(action, event);

    // advise casesvc to create a corresponding caseevent for our action
    caseSvcClientService.createNewCaseEvent(action, CategoryDTO.CategoryName.ACTION_CREATED);

    actionInstructionPublisher.sendActionInstruction(action.getActionType().getHandler(), prepareActionRequest(action));
  }

  /**
   * Deal with a single action cancel - the transaction boundary is here
   *
   * @param action the action to deal with
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  private void processActionCancel(final Action action) throws CTPException {
    log.info("processing action REQUEST actionid {} caseid {} actionplanid {}", action.getActionPK(),
        action.getCaseId(), action.getActionPlanFK());

    transitionAction(action, ActionDTO.ActionEvent.CANCELLATION_DISTRIBUTED);

    // advise casesvc to create a corresponding caseevent for our action
    caseSvcClientService.createNewCaseEvent(action, CategoryDTO.CategoryName.ACTION_CANCELLATION_CREATED);

    actionInstructionPublisher.sendActionInstruction(action.getActionType().getHandler(), prepareActionCancel(action));
  }

  /**
   * Change the action status in db to indicate we have sent this action
   * downstream, and clear previous situation (in the scenario where the action
   * has prev. failed)
   *
   * @param action the action to change and persist
   * @param event the event to transition the action with
   * @throws CTPException if action state transition error
   */
  private void transitionAction(final Action action, final ActionDTO.ActionEvent event) throws CTPException {
    ActionDTO.ActionState nextState = actionSvcStateTransitionManager.transition(action.getState(), event);
    action.setState(nextState);
    action.setSituation(null);
    action.setUpdatedDateTime(DateTimeUtil.nowUTC());
    actionRepo.saveAndFlush(action);
  }

  /**
   * Take an action and using it, fetch further info from Case service in a
   * number of rest calls, in order to create the ActionRequest
   *
   * @param action It all starts with the Action
   * @return The ActionRequest created from the Action and the other info from
   *         CaseSvc
   */
  private ActionRequest prepareActionRequest(final Action action) {
    log.debug("constructing ActionRequest to publish to downstream handler for action id {} and case id {}",
        action.getActionPK(), action.getCaseId());
    // now call caseSvc for the following
    ActionPlan actionPlan = (action.getActionPlanFK() == null) ? null
        : actionPlanRepo.findOne(action.getActionPlanFK());
    CaseDetailsDTO caseDTO = caseSvcClientService.getCaseWithIACandCaseEvents(action.getCaseId());

    PartyDTO partyDTO = partySvcClientService.getParty(caseDTO.getSampleUnitType(), caseDTO.getPartyId());
    log.debug("PARTYDTO: " + partyDTO.toString());

    //List<CaseEventDTO> caseEventDTOs = caseSvcClientService.getCaseEvents(action.getCaseId());
    List<CaseEventDTO> caseEventDTOs = caseDTO.getCaseEvents();


    return createActionRequest(action, actionPlan, caseDTO, partyDTO, caseEventDTOs);
  }

  /**
   * Take an action and using it, fetch further info from Case service in a
   * number of rest calls, in order to create the ActionRequest
   *
   * @param action It all starts wih the Action
   * @return The ActionRequest created from the Action and the other info from
   *         CaseSvc
   */
  private ActionCancel prepareActionCancel(final Action action) {
    log.debug("constructing ActionCancel to publish to downstream handler for action id {} and case id {}",
        action.getActionPK(), action.getCaseId());
    ActionCancel actionCancel = new ActionCancel();
    actionCancel.setActionId(action.getId().toString());
    actionCancel.setResponseRequired(true);
    actionCancel.setReason("Action cancelled by Response Management");
    return actionCancel;
  }

  /**
   * Given the business objects passed, create, populate and return an
   * ActionRequest
   *
   * @param action the persistent Action obj from the db
   * @param actionPlan the persistent ActionPlan obj from the db
   * @param caseDTO the Case representation from the CaseSvc
   * @param partyDTO the Party containing the Address representation from the PartySvc
   * @param caseEventDTOs the list of CaseEvent representations from the CaseSvc
   * @return the shiney new Action Request
   */
  private ActionRequest createActionRequest(final Action action, final ActionPlan actionPlan,
      final CaseDetailsDTO caseDTO,
      final PartyDTO partyDTO,
      final List<CaseEventDTO> caseEventDTOs) {
    ActionRequest actionRequest = new ActionRequest();
    // populate the request
    actionRequest.setActionId(action.getId().toString());
    actionRequest.setActionPlan((actionPlan == null) ? null : actionPlan.getName());
    actionRequest.setActionType(action.getActionType().getName());
    // TODO BRES where does questionSet come from now?!
//    actionRequest.setQuestionSet(caseTypeDTO.getQuestionSet());
    actionRequest.setResponseRequired(action.getActionType().getResponseRequired());
    actionRequest.setCaseId(action.getCaseId().toString());
  
    UUID collectionId = caseDTO.getCaseGroup().getCollectionExerciseId();
    CollectionExerciseDTO collectionExe =  collectionExerciseClientService.getCollectionExercise(collectionId);
    actionRequest.setExerciseRef(collectionExe.getExerciseRef());
    
    Map<String, String> partyMap = partyDTO.getAttributes();

    ActionContact actionContact = new ActionContact();
    //actionContact.setTitle(partyMap.get("title")); //TODO Not in Party Swagger Spec.
    // TODO Bad practice to have the string below hardcoded
    actionContact.setForename(partyMap.get("surname"));
    actionContact.setPhoneNumber(partyMap.get("phonenumber"));
    actionContact.setEmailAddress(partyMap.get("emailAddress"));
    actionRequest.setContact(actionContact);

    ActionEvent actionEvent = new ActionEvent();
    caseEventDTOs.forEach((caseEventDTO) -> actionEvent.getEvents().add(formatCaseEvent(caseEventDTO)));
    actionRequest.setEvents(actionEvent);
    actionRequest.setIac(caseDTO.getIac());
    actionRequest.setPriority(Priority.fromValue(ActionPriority.valueOf(action.getPriority()).getName()));
  //  actionRequest.setCaseRef(caseDTO.getCaseRef());

    ActionAddress actionAddress = new ActionAddress();
    mapperFacade.map(partyDTO, ActionAddress.class);
    actionAddress.setSampleUnitRef(caseDTO.getCaseGroup().getSampleUnitRef());
    actionRequest.setAddress(actionAddress);
    return actionRequest;
  }

  /**
   * Formats a CaseEvent as a string that can added to the ActionRequest
   *
   * @param caseEventDTO the DTO to be formatted
   * @return the pretty one liner
   */
  private String formatCaseEvent(final CaseEventDTO caseEventDTO) {
    return String.format("%s : %s : %s : %s", caseEventDTO.getCategory(), caseEventDTO.getSubCategory(),
        caseEventDTO.getCreatedBy(), caseEventDTO.getDescription());
  }

}
