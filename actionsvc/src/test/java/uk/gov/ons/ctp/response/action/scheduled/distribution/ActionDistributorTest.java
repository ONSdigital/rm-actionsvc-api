package uk.gov.ons.ctp.response.action.scheduled.distribution;

import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.action.config.ActionDistribution;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.config.CaseSvc;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.domain.model.ActionType;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionTypeRepository;
import uk.gov.ons.ctp.response.action.message.ActionInstructionPublisher;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.representation.ActionDTO.ActionState;
import uk.gov.ons.ctp.response.action.service.CaseSvcClientService;
import uk.gov.ons.ctp.response.action.service.CollectionExerciseClientService;
import uk.gov.ons.ctp.response.action.service.impl.PartySvcClientServiceImpl;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDetailsDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CategoryDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the ActionDistributor
 *
 * Important reminder on the standing data held in json files:
 *  - case 3382981d-3df0-464e-9c95-aea7aee80c81 is linked with a SUBMITTED action so expect 1 ActionRequest
 *  - case 3382981d-3df0-464e-9c95-aea7aee80c82 is linked with a CANCEL_SUBMITTED action so expect 1 ActionCancel
 *  - case 3382981d-3df0-464e-9c95-aea7aee80c83 is linked with a SUBMITTED action so expect 1 ActionRequest
 *  - case 3382981d-3df0-464e-9c95-aea7aee80c84 is linked with a CANCEL_SUBMITTED action so expect 1 ActionCancel
 *  - all actions have responseRequired = true
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionDistributorTest {

  private static final int TEN = 10;

  private static final String CONNECTIVITY_ISSUE = "Connectivity issue";
  private static final String HOUSEHOLD_INITIAL_CONTACT = "HouseholdInitialContact";
  private static final String HOUSEHOLD_UPLOAD_IAC = "HouseholdUploadIAC";

  private static final UUID CASE_ID_1_ISSUE_WITH_CASESVC = UUID.fromString("3382981d-3df0-464e-9c95-aea7aee80c81");
  private static final UUID CASE_ID_2_ISSUE_WITH_CASESVC = UUID.fromString("3382981d-3df0-464e-9c95-aea7aee80c83");
  private static final UUID CASE_ID_1 = UUID.fromString("3382981d-3df0-464e-9c95-aea7aee80c82");
  private static final UUID CASE_ID_2 = UUID.fromString("3382981d-3df0-464e-9c95-aea7aee80c84");

  private List<ActionType> actionTypes;
  private List<Action> householdInitialContactActions;
  private List<Action> householdUploadIACActions;
  private List<CaseDetailsDTO> caseDetailsDTOs;
  private List<PartyDTO> partyDTOs;
  private List<CollectionExerciseDTO> collectionExerciseDTOs;

  @Spy
  private AppConfig appConfig = new AppConfig();

  @Mock
  private ActionInstructionPublisher actionInstructionPublisher;

  @Mock
  private DistributedListManager<BigInteger> actionDistributionListManager;

  @Mock
  private StateTransitionManager<ActionState, uk.gov.ons.ctp.response.action.representation.ActionDTO.ActionEvent>
          actionSvcStateTransitionManager;

  @Mock
  private MapperFacade mapperFacade;

  @Mock
  private CaseSvcClientService caseSvcClientService;

  @Mock
  private CollectionExerciseClientService collectionExerciseClientService;

  @Mock
  private ActionRepository actionRepo;

  @Mock
  private ActionPlanRepository actionPlanRepo;

  @Mock
  private ActionTypeRepository actionTypeRepo;

  @Mock
  private TransactionTemplate transactionTemplate;

  @Mock
  private PlatformTransactionManager platformTransactionManager;

  @Mock
  private PartySvcClientServiceImpl partySvcClientService;

  @InjectMocks
  private ActionDistributor actionDistributor;

  /**
   * Initialises Mockito and loads Class Fixtures
   */
  @Before
  public void setUp() throws Exception {
    CaseSvc caseSvcConfig = new CaseSvc();
    appConfig.setCaseSvc(caseSvcConfig);

    ActionDistribution actionDistributionConfig = new ActionDistribution();
    actionDistributionConfig.setDelayMilliSeconds(TEN);
    actionDistributionConfig.setRetrievalMax(TEN);
    actionDistributionConfig.setRetrySleepSeconds(TEN);
    appConfig.setActionDistribution(actionDistributionConfig);

    actionTypes = FixtureHelper.loadClassFixtures(ActionType[].class);
    householdInitialContactActions = FixtureHelper.loadClassFixtures(Action[].class, HOUSEHOLD_INITIAL_CONTACT);
    householdUploadIACActions = FixtureHelper.loadClassFixtures(Action[].class, HOUSEHOLD_UPLOAD_IAC);
    partyDTOs = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    caseDetailsDTOs = FixtureHelper.loadClassFixtures(CaseDetailsDTO[].class);
    collectionExerciseDTOs = FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class);

    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test that when we fail at first hurdle to load ActionTypes we do not go on to call anything else. In reality, the
   * wakeup method would then be called again after a sleep interval by Spring but we cannot test that here.
   *
   * @throws Exception oops
   */
  @Test
  public void testFailToGetAnyActionType() throws Exception {
    Mockito.when(actionTypeRepo.findAll()).thenThrow(new RuntimeException("Database access failed"));

    DistributionInfo info = actionDistributor.distribute();
    List<InstructionCount> countList = info.getInstructionCounts();
    assertTrue(countList.isEmpty());

    verify(actionTypeRepo).findAll();

    // Assertions for calls in method retrieveActions
    verify(actionDistributionListManager, times(0)).findList(any(String.class),
        any(Boolean.class));
    verify(actionRepo, times(0)).findByActionTypeNameAndStateInAndActionPKNotIn(
        any(String.class), anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionDistributionListManager, times(0)).saveList(any(String.class), any(List.class),
        any(Boolean.class));

    // Assertions for calls in processActionRequest or processActionCancel
    verify(actionSvcStateTransitionManager, times(0)).transition(any(ActionState.class),
        any(ActionDTO.ActionEvent.class));
    verify(caseSvcClientService, times(0)).createNewCaseEvent(any(Action.class),
        any(CategoryDTO.CategoryName.class));

    // Assertion on what is sent to queue
    verify(actionInstructionPublisher, times(0)).sendActionInstruction(any(String.class),
        any(uk.gov.ons.ctp.response.action.message.instruction.Action.class));
  }

  /**
   * We retrieve ok actionTypes but then exception thrown when retrieving actions.
   *
   * @throws Exception oops
   */
  @Test
  public void testFailToGetAnyAction() throws Exception {
    Mockito.when(actionTypeRepo.findAll()).thenReturn(actionTypes);
    Mockito.when(actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(any(String.class), any(List.class),
        any(List.class), any(Pageable.class))).thenThrow(new RuntimeException("Database access failed"));

    DistributionInfo info = actionDistributor.distribute();
    List<InstructionCount> countList = info.getInstructionCounts();
    assertEquals(4, countList.size());

    List<InstructionCount> expectedCountList = new ArrayList<>();
    expectedCountList.add(new InstructionCount(HOUSEHOLD_INITIAL_CONTACT,
        DistributionInfo.Instruction.REQUEST, 0));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_INITIAL_CONTACT,
        DistributionInfo.Instruction.CANCEL_REQUEST, 0));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_UPLOAD_IAC,
        DistributionInfo.Instruction.REQUEST, 0));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_UPLOAD_IAC,
        DistributionInfo.Instruction.CANCEL_REQUEST, 0));
    assertTrue(countList.equals(expectedCountList));

    // Assertions for calls in method retrieveActions
    verify(actionDistributionListManager).findList(eq(HOUSEHOLD_INITIAL_CONTACT), eq(false));
    verify(actionDistributionListManager).findList(eq(HOUSEHOLD_UPLOAD_IAC), eq(false));
    verify(actionRepo, times(1)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq(HOUSEHOLD_INITIAL_CONTACT), anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionRepo, times(1)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq(HOUSEHOLD_UPLOAD_IAC), anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionDistributionListManager, times(0)).saveList(anyString(), anyList(),
        anyBoolean());

    // Assertions for calls in processActionRequest or processActionCancel
    verify(actionSvcStateTransitionManager, times(0)).transition(any(ActionState.class),
        any(ActionDTO.ActionEvent.class));
    verify(caseSvcClientService, times(0)).createNewCaseEvent(any(Action.class),
        any(CategoryDTO.CategoryName.class));

    // Assertion on what is sent to queue
    verify(actionInstructionPublisher, times(0)).sendActionInstruction(any(String.class),
        any(uk.gov.ons.ctp.response.action.message.instruction.Action.class));
  }

  /**
   * Happy Path with 2 ActionRequests and 2 ActionCancels
   *
   * @throws Exception oops
   */
  @Test
  public void testHappyPath() throws Exception {
    Mockito.when(actionTypeRepo.findAll()).thenReturn(actionTypes);
    Mockito.when(actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq(HOUSEHOLD_INITIAL_CONTACT),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class))).thenReturn(
            householdInitialContactActions);
    Mockito.when(actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq(HOUSEHOLD_UPLOAD_IAC),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class))).thenReturn(
        householdUploadIACActions);
    Mockito.when(actionSvcStateTransitionManager.transition(ActionState.SUBMITTED,
        ActionDTO.ActionEvent.REQUEST_DISTRIBUTED)).thenReturn(ActionState.PENDING);
    Mockito.when(actionSvcStateTransitionManager.transition(ActionState.CANCEL_SUBMITTED,
        ActionDTO.ActionEvent.CANCELLATION_DISTRIBUTED)).thenReturn(ActionState.CANCEL_PENDING);
    Mockito.when(caseSvcClientService.getCaseWithIACandCaseEvents(any(UUID.class))).thenReturn(caseDetailsDTOs.get(0));
    Mockito.when(partySvcClientService.getParty(any(String.class), any(UUID.class))).thenReturn(partyDTOs.get(0));
    Mockito.when(collectionExerciseClientService.getCollectionExercise(any(UUID.class))).
            thenReturn(collectionExerciseDTOs.get(0));

    DistributionInfo info = actionDistributor.distribute();
    List<InstructionCount> countList = info.getInstructionCounts();
    assertEquals(4, countList.size());
    List<InstructionCount> expectedCountList = new ArrayList<>();
    expectedCountList.add(new InstructionCount(HOUSEHOLD_INITIAL_CONTACT,
        DistributionInfo.Instruction.REQUEST, 1));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_INITIAL_CONTACT,
        DistributionInfo.Instruction.CANCEL_REQUEST, 1));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_UPLOAD_IAC,
        DistributionInfo.Instruction.REQUEST, 1));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_UPLOAD_IAC,
        DistributionInfo.Instruction.CANCEL_REQUEST, 1));
    assertTrue(countList.equals(expectedCountList));

    verify(actionTypeRepo).findAll();

    // Assertions for calls in method retrieveActions
    verify(actionDistributionListManager).findList(eq(HOUSEHOLD_INITIAL_CONTACT), eq(false));
    verify(actionDistributionListManager).findList(eq(HOUSEHOLD_UPLOAD_IAC), eq(false));
    verify(actionRepo, times(1)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq(HOUSEHOLD_INITIAL_CONTACT), anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionRepo, times(1)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq(HOUSEHOLD_UPLOAD_IAC), anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionDistributionListManager).saveList(eq(HOUSEHOLD_INITIAL_CONTACT), anyList(), anyBoolean());
    verify(actionDistributionListManager).saveList(eq(HOUSEHOLD_UPLOAD_IAC), anyList(), anyBoolean());

    // Assertions for calls in processActionRequest
    verify(actionSvcStateTransitionManager, times(2)).transition(ActionState.SUBMITTED,
        ActionDTO.ActionEvent.REQUEST_DISTRIBUTED);
    verify(caseSvcClientService, times(2)).createNewCaseEvent(any(Action.class),
        eq(CategoryDTO.CategoryName.ACTION_CREATED));
    verify(actionPlanRepo, times(2)).findOne(any(Integer.class));
    verify(caseSvcClientService, times(2)).getCaseWithIACandCaseEvents(any(UUID.class));
    verify(partySvcClientService, times(2)).getParty(any(String.class), any(UUID.class));
    verify(collectionExerciseClientService, times(2)).getCollectionExercise(any(UUID.class));

    // Assertions for calls in processActionCancel
    verify(actionSvcStateTransitionManager, times(2)).transition(ActionState.CANCEL_SUBMITTED,
        ActionDTO.ActionEvent.CANCELLATION_DISTRIBUTED);
    verify(caseSvcClientService, times(2)).createNewCaseEvent(any(Action.class),
        eq(CategoryDTO.CategoryName.ACTION_CANCELLATION_CREATED));

    // Assertion on what is sent to queue
    verify(actionInstructionPublisher, times(4)).sendActionInstruction(any(String.class),
        any(uk.gov.ons.ctp.response.action.message.instruction.Action.class));
  }

  /**
   * Scenario where an exception is thrown when calling the CaseService for 2 of our 4 actions:
   *    - case with id 3382981d-3df0-464e-9c95-aea7aee80c81 (linked with a SUBMITTED action)
   *    - case with id 3382981d-3df0-464e-9c95-aea7aee80c83 (linked with a SUBMITTED action)
   *
   * @throws Exception oops
   */
  @Test
  public void testCaseServiceIssue() throws Exception {
    Mockito.when(actionTypeRepo.findAll()).thenReturn(actionTypes);
    Mockito.when(actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq(HOUSEHOLD_INITIAL_CONTACT),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class))).thenReturn(
        householdInitialContactActions);
    Mockito.when(actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq(HOUSEHOLD_UPLOAD_IAC),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class))).thenReturn(
        householdUploadIACActions);
    Mockito.when(actionSvcStateTransitionManager.transition(ActionState.SUBMITTED,
        ActionDTO.ActionEvent.REQUEST_DISTRIBUTED)).thenReturn(ActionState.PENDING);

    Mockito.when(caseSvcClientService.getCaseWithIACandCaseEvents(CASE_ID_1)).thenReturn(caseDetailsDTOs.get(0));
    Mockito.when(caseSvcClientService.getCaseWithIACandCaseEvents(CASE_ID_2)).thenReturn(caseDetailsDTOs.get(0));
    Mockito.when(caseSvcClientService.getCaseWithIACandCaseEvents(CASE_ID_1_ISSUE_WITH_CASESVC))
        .thenThrow(new RuntimeException(CONNECTIVITY_ISSUE));
    Mockito.when(caseSvcClientService.getCaseWithIACandCaseEvents(CASE_ID_2_ISSUE_WITH_CASESVC))
        .thenThrow(new RuntimeException(CONNECTIVITY_ISSUE));

    Mockito.when(partySvcClientService.getParty(any(String.class), any(UUID.class))).thenReturn(partyDTOs.get(0));
    Mockito.when(collectionExerciseClientService.getCollectionExercise(any(UUID.class))).
        thenReturn(collectionExerciseDTOs.get(0));

    DistributionInfo info = actionDistributor.distribute();
    List<InstructionCount> countList = info.getInstructionCounts();
    assertEquals(4, countList.size());
    List<InstructionCount> expectedCountList = new ArrayList<>();
    expectedCountList.add(new InstructionCount(HOUSEHOLD_INITIAL_CONTACT,
        DistributionInfo.Instruction.REQUEST, 0));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_INITIAL_CONTACT,
        DistributionInfo.Instruction.CANCEL_REQUEST, 1));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_UPLOAD_IAC,
        DistributionInfo.Instruction.REQUEST, 0));
    expectedCountList.add(new InstructionCount(HOUSEHOLD_UPLOAD_IAC,
        DistributionInfo.Instruction.CANCEL_REQUEST, 1));
    assertTrue(countList.equals(expectedCountList));

    verify(actionTypeRepo).findAll();

    // Assertions for calls in method retrieveActions
    verify(actionDistributionListManager).findList(eq(HOUSEHOLD_INITIAL_CONTACT), eq(false));
    verify(actionDistributionListManager).findList(eq(HOUSEHOLD_UPLOAD_IAC), eq(false));
    verify(actionRepo, times(1)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq(HOUSEHOLD_INITIAL_CONTACT), anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionRepo, times(1)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq(HOUSEHOLD_UPLOAD_IAC), anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionDistributionListManager).saveList(eq(HOUSEHOLD_INITIAL_CONTACT), anyList(), anyBoolean());
    verify(actionDistributionListManager).saveList(eq(HOUSEHOLD_UPLOAD_IAC), anyList(), anyBoolean());

    // Assertions for calls in processActionRequest
    verify(actionSvcStateTransitionManager, times(2)).transition(ActionState.SUBMITTED,
        ActionDTO.ActionEvent.REQUEST_DISTRIBUTED);
    verify(caseSvcClientService, times(2)).createNewCaseEvent(any(Action.class),
        eq(CategoryDTO.CategoryName.ACTION_CREATED));
    verify(actionPlanRepo, times(2)).findOne(any(Integer.class));
    verify(caseSvcClientService, times(2)).getCaseWithIACandCaseEvents(any(UUID.class));
    verify(partySvcClientService, times(0)).getParty(any(String.class), any(UUID.class));
    verify(collectionExerciseClientService, times(0)).getCollectionExercise(any(UUID.class));

    // Assertions for calls in processActionCancel
    verify(actionSvcStateTransitionManager, times(2)).transition(ActionState.CANCEL_SUBMITTED,
        ActionDTO.ActionEvent.CANCELLATION_DISTRIBUTED);
    verify(caseSvcClientService, times(2)).createNewCaseEvent(any(Action.class),
        eq(CategoryDTO.CategoryName.ACTION_CANCELLATION_CREATED));

    // Assertion on what is sent to queue
    verify(actionInstructionPublisher, times(2)).sendActionInstruction(any(String.class),
        any(uk.gov.ons.ctp.response.action.message.instruction.Action.class));
  }
}
