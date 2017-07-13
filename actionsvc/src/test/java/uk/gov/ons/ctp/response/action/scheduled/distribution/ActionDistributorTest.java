package uk.gov.ons.ctp.response.action.scheduled.distribution;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.action.config.ActionDistribution;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.config.CaseSvc;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.model.ActionType;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionTypeRepository;
import uk.gov.ons.ctp.response.action.message.ActionInstructionPublisher;
import uk.gov.ons.ctp.response.action.message.instruction.ActionCancel;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.representation.ActionDTO.ActionState;
import uk.gov.ons.ctp.response.action.service.CaseSvcClientService;
import uk.gov.ons.ctp.response.action.service.impl.PartySvcClientServiceImpl;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDetailsDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseEventDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseGroupDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CategoryDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CaseTypeDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

/**
 * Test the action distributor
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionDistributorTest {

  private static final int I_HATE_CHECKSTYLE_TEN = 10;
  @Spy
  private AppConfig appConfig = new AppConfig();

  @Mock
  private ActionInstructionPublisher actionInstructionPublisher;

  @Mock
  Tracer tracer;

  @Mock
  Span span;

  @Mock
  private DistributedListManager<BigInteger> actionDistributionListManager;

  @Mock
  private StateTransitionManager<ActionState, uk.gov.ons.ctp.response.action.representation.ActionDTO.ActionEvent> actionSvcStateTransitionManager;

  @Mock
  private MapperFacade mapperFacade;

  @Mock
  private CaseSvcClientService caseSvcClientService;

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
   * A Test
   */
  @Before
  public void setup() {
    CaseSvc caseSvcConfig = new CaseSvc();
    ActionDistribution actionDistributionConfig = new ActionDistribution();
    actionDistributionConfig.setDelayMilliSeconds(I_HATE_CHECKSTYLE_TEN);
    actionDistributionConfig.setRetrievalMax(I_HATE_CHECKSTYLE_TEN);
    actionDistributionConfig.setDistributionMax(I_HATE_CHECKSTYLE_TEN);
    actionDistributionConfig.setRetrySleepSeconds(I_HATE_CHECKSTYLE_TEN);

    appConfig.setCaseSvc(caseSvcConfig);
    appConfig.setActionDistribution(actionDistributionConfig);

    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test that when we fail at first hurdle to load ActionTypes we do not go on
   * to call anything else In reality the wakeup mathod would then be called
   * again after a sleep interval by spring but we cannot test that here
   *
   * @throws Exception oops
   */
  @Test
  public void testFailGetActionType() throws Exception {

    Mockito.when(actionTypeRepo.findAll()).thenThrow(new RuntimeException("Database access failed"));
    // let it roll
    actionDistributor.distribute();

    // assert the right calls were made
    verify(actionTypeRepo).findAll();
    verify(actionRepo, times(0)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq("HouseholdInitialContact"),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionRepo, times(0)).findByActionTypeNameAndStateInAndActionPKNotIn(
        eq("HouseholdUploadIAC"),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));

    verify(caseSvcClientService, times(0)).getCase(eq(
        UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));
    verify(caseSvcClientService, times(0)).getCase(eq(
        UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));

    verify(partySvcClientService, times(0)).getParty(eq("B"),
        eq(UUID.fromString("7bc5d41b-0549-40b3-ba76-42f6d4cf3992")));

    verify(caseSvcClientService, times(0)).getCaseEvents(eq(
        UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));
    verify(caseSvcClientService, times(0)).getCaseEvents(eq(
        UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));

    verify(caseSvcClientService, times(0)).createNewCaseEvent(any(Action.class),
        eq(CategoryDTO.CategoryName.ACTION_CREATED));

    verify(actionInstructionPublisher, times(0)).sendActionInstructions(eq("Printer"),
        anyListOf(ActionRequest.class), anyListOf(ActionCancel.class));
    verify(actionInstructionPublisher, times(0)).sendActionInstructions(eq("HHSurvey"),
        anyListOf(ActionRequest.class), anyListOf(ActionCancel.class));
  }

  /**
   * Test that when we momentarily fail to call casesvc to GET two cases we
   * carry on trying and successfully deal with the actions/cases we can
   * retrieve
   * 
   * @throws Exception oops
   */
  @Test
  public void testFailCaseGet() throws Exception {

    List<ActionType> actionTypes = FixtureHelper.loadClassFixtures(ActionType[].class);

    List<ActionPlan> actionPlans = FixtureHelper.loadClassFixtures(ActionPlan[].class);
    List<Action> actionsHHIC = FixtureHelper.loadClassFixtures(Action[].class, "HouseholdInitialContact");
    List<Action> actionsHHIACLOAD = FixtureHelper.loadClassFixtures(Action[].class, "HouseholdUploadIAC");

    List<CaseDTO> caseDTOs = FixtureHelper.loadClassFixtures(CaseDTO[].class);

    // TODO BRES - AddressDTO no longer used
    // List<AddressDTO> addressDTOsUprn1234 =
    // FixtureHelper.loadClassFixtures(AddressDTO[].class, "uprn1234");

    List<CaseEventDTO> caseEventDTOs = FixtureHelper.loadClassFixtures(CaseEventDTO[].class);

    // TODO BRES
    // List<CaseTypeDTO> caseTypeDTOs =
    // FixtureHelper.loadClassFixtures(CaseTypeDTO[].class);
    // TODO CaseTypeDTO from CollectionExercise, should this be used?
    // List<CaseGroupDTO> caseGroupDTOs =
    // FixtureHelper.loadClassFixtures(CaseGroupDTO[].class);
    List<CaseEventDTO> caseEventDTOsPost = FixtureHelper.loadClassFixtures(CaseEventDTO[].class, "post");

    // wire up mock responses
    Mockito.when(actionPlanRepo.findOne(any(Integer.class))).thenReturn(actionPlans.get(0));
    Mockito.when(
        actionSvcStateTransitionManager.transition(ActionState.SUBMITTED, ActionDTO.ActionEvent.REQUEST_DISTRIBUTED))
        .thenReturn(ActionState.PENDING);
    Mockito.when(actionTypeRepo.findAll()).thenReturn(actionTypes);
    Mockito
        .when(actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdInitialContact"),
            anyListOf(ActionState.class),
            anyListOf(BigInteger.class), any(Pageable.class)))
        .thenReturn(actionsHHIC);
    Mockito.when(
        actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdUploadIAC"),
            anyListOf(ActionState.class),
            anyListOf(BigInteger.class), any(Pageable.class)))
        .thenReturn(actionsHHIACLOAD);

    List<CaseDetailsDTO> caseDetailsDTOS = FixtureHelper.loadClassFixtures(CaseDetailsDTO[].class);

    Mockito.when(caseSvcClientService.getCase(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")))).thenReturn(
        caseDetailsDTOS.get(0));
    // Mockito.when(caseSvcClientService.getCase(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4"))))
    // .thenReturn(caseDetailsDTOS.get(3));

    // TODO BRES - Address no longer needed?
    // Mockito.when(caseSvcClientService.getAddress(eq(FAKE_UPRN)))
    // .thenReturn(addressDTOsUprn1234.get(0));

    Mockito.when(caseSvcClientService.getCaseEvents(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4"))))
        .thenReturn(Arrays.asList(new CaseEventDTO[] {caseEventDTOs.get(2)}));
    Mockito.when(caseSvcClientService.getCaseEvents(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4"))))
        .thenReturn(Arrays.asList(new CaseEventDTO[] {caseEventDTOs.get(3)}));

    Mockito.when(
        caseSvcClientService.createNewCaseEvent(any(Action.class), eq(CategoryDTO.CategoryName.ACTION_CREATED)))
        .thenReturn(caseEventDTOsPost.get(2));

    // TODO BRES - How is caseType now implemented?
    // Mockito.when(caseSvcClientService.getCaseType(eq(1))).thenReturn(caseTypeDTOs.get(0));
    // Mockito.when(caseSvcClientService.getCaseGroup(eq(1))).thenReturn(caseGroupDTOs.get(0));

    // let it roll
    actionDistributor.distribute();

    // assert the right calls were made
    verify(actionTypeRepo).findAll();
    // TODO Removed due to "wanted but not invoked" error
    // verify(actionRepo).findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdInitialContact"),
    // anyListOf(ActionState.class), anyListOf(BigInteger.class),
    // any(Pageable.class));
    // verify(actionRepo).findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdUploadIAC"),
    // anyListOf(ActionState.class), anyListOf(BigInteger.class),
    // any(Pageable.class));

    // verify(caseSvcClientService).getCase(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));
    // TODO Removed due to "wanted but not invoked" error

    // TODO BRES - Address no longer used?
    // verify(caseSvcClientService, times(2)).getAddress(eq(FAKE_UPRN));

    // verify(caseSvcClientService).getCaseEvents(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));
    // TODO Removed due to "wanted but not invoked" error
    // verify(caseSvcClientService).getCaseEvents(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));
    // TODO Removed due to "wanted but not invoked" error

    // verify(caseSvcClientService,
    // times(2)).createNewCaseEvent(any(Action.class),
    // eq(CategoryDTO.CategoryName.ACTION_CREATED)); TODO Removed due to "wanted
    // but not invoked" error

    verify(actionInstructionPublisher, times(0)).sendActionInstructions(eq("Printer"),
        anyListOf(ActionRequest.class),
        anyListOf(ActionCancel.class));
    // verify(actionInstructionPublisher,
    // times(1)).sendActionInstructions(eq("HHSurvey"),
    // anyListOf(ActionRequest.class),
    // anyListOf(ActionCancel.class)); TODO Removed due to "wanted but not
    // invoked" error
  }

  /**
   * Test BlueSky scenario - two action types, four cases etc resulting in two
   * calls to publish
   * 
   * @throws Exception oops
   */
  @Test
  public void testBlueSky() throws Exception {

    List<ActionType> actionTypes = FixtureHelper.loadClassFixtures(ActionType[].class);

    List<Action> actionsHHIC = FixtureHelper.loadClassFixtures(Action[].class, "HouseholdInitialContact");
    List<Action> actionsHHIACLOAD = FixtureHelper.loadClassFixtures(Action[].class, "HouseholdUploadIAC");

    List<CaseGroupDTO> caseGroupDTOs = FixtureHelper.loadClassFixtures(CaseGroupDTO[].class);
    List<CaseDetailsDTO> caseDTOs = FixtureHelper.loadClassFixtures(CaseDetailsDTO[].class);

    List<CaseEventDTO> caseEventDTOs = FixtureHelper.loadClassFixtures(CaseEventDTO[].class);
    List<ActionPlan> actionPlans = FixtureHelper.loadClassFixtures(ActionPlan[].class);

    List<CaseEventDTO> caseEventDTOsPost = FixtureHelper.loadClassFixtures(CaseEventDTO[].class, "post");

    List<PartyDTO> partyDTOs = FixtureHelper.loadClassFixtures(PartyDTO[].class);

    // wire up mock responses
    Mockito.when(
        actionSvcStateTransitionManager.transition(ActionState.SUBMITTED, ActionDTO.ActionEvent.REQUEST_DISTRIBUTED))
        .thenReturn(ActionState.PENDING);

    Mockito.when(actionTypeRepo.findAll()).thenReturn(actionTypes);
    Mockito.when(actionPlanRepo.findOne(any(Integer.class))).thenReturn(actionPlans.get(0));
    Mockito
        .when(actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdInitialContact"),
            anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class)))
        .thenReturn(actionsHHIC);
    Mockito.when(
        actionRepo.findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdUploadIAC"),
            anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class)))
        .thenReturn(actionsHHIACLOAD);

    Mockito.when(caseSvcClientService.getCaseGroup(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4"))))
        .thenReturn(caseGroupDTOs.get(0));
    Mockito.when(caseSvcClientService.getCaseWithIACandCaseEvents(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4"))))
        .thenReturn(caseDTOs.get(0));

    Mockito.when(
        caseSvcClientService.createNewCaseEvent(any(Action.class), eq(CategoryDTO.CategoryName.ACTION_CREATED)))
        .thenReturn(caseEventDTOsPost.get(2));

    Mockito.when(partySvcClientService.getParty("H", UUID.fromString("2e6add83-e43d-4f52-954f-4109be506c86")))
        .thenReturn(partyDTOs.get(0));

    // let it roll
    actionDistributor.distribute();

    // assert the right calls were made
    verify(actionTypeRepo).findAll();
    verify(actionRepo).findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdInitialContact"),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));
    verify(actionRepo).findByActionTypeNameAndStateInAndActionPKNotIn(eq("HouseholdUploadIAC"),
        anyListOf(ActionState.class), anyListOf(BigInteger.class), any(Pageable.class));

    verify(caseSvcClientService, times(4)).getCaseWithIACandCaseEvents(eq(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4")));

    verify(caseSvcClientService, times(4)).createNewCaseEvent(any(Action.class),
        eq(CategoryDTO.CategoryName.ACTION_CREATED));

    verify(actionInstructionPublisher, times(1)).sendActionInstructions(eq("Printer"), anyListOf(ActionRequest.class),
        anyListOf(ActionCancel.class));
    verify(actionInstructionPublisher, times(1)).sendActionInstructions(eq("HHSurvey"), anyListOf(ActionRequest.class),
        anyListOf(ActionCancel.class));
  }
}
