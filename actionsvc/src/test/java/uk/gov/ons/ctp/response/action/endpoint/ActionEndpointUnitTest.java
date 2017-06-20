package uk.gov.ons.ctp.response.action.endpoint;

import ma.glasnost.orika.MapperFacade;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.action.ActionBeanMapper;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.domain.model.ActionCase;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.model.ActionType;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.service.ActionCaseService;
import uk.gov.ons.ctp.response.action.service.ActionPlanService;
import uk.gov.ons.ctp.response.action.service.ActionService;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.common.MvcHelper.*;
import static uk.gov.ons.ctp.common.error.RestExceptionHandler.INVALID_JSON;
import static uk.gov.ons.ctp.common.error.RestExceptionHandler.PROVIDED_JSON_INCORRECT;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static uk.gov.ons.ctp.response.action.service.impl.ActionPlanJobServiceImpl.CREATED_BY_SYSTEM;

/**
 * ActionEndpoint Unit tests
 */
public final class ActionEndpointUnitTest {

  @InjectMocks
  private ActionEndpoint actionEndpoint;

  @Mock
  private ActionService actionService;

  @Mock
  private ActionPlanService actionPlanService;

  @Mock
  private ActionCaseService actionCaseService;

  @Spy
  private MapperFacade mapperFacade = new ActionBeanMapper();

  private MockMvc mockMvc;

  private List<Action> actions;
  private List<ActionPlan> actionPlans;

  private static final ActionDTO.ActionState ACTION2_ACTIONSTATE = ActionDTO.ActionState.COMPLETED;
  private static final ActionDTO.ActionState ACTION3_ACTIONSTATE = ActionDTO.ActionState.CANCELLED;

  private static final Integer ACTION_CASEFK = 1;
  private static final Integer ACTION2_PRIORITY = 3;
  private static final Integer ACTION2_PLAN_FK = 2;
  private static final Integer ACTION2_RULE_FK = 2;
  private static final String NON_EXISTING_ID = "e1c26bf2-eaa8-4a8a-b44f-3b8f004ef271";

  private static final BigInteger ACTION_PK = BigInteger.valueOf(1);

  private static final UUID ACTION_ID_1 = UUID.fromString("d24b3f17-bbf8-4c71-b2f0-a4334125d78a");
  private static final UUID ACTION_ID_1_CASE_ID = UUID.fromString("7bc5d41b-0549-40b3-ba76-42f6d4cf3fda");
  private static final UUID ACTION_ID_2 = UUID.fromString("d24b3f17-bbf8-4c71-b2f0-a4334125d78b");
  private static final UUID ACTION_ID_2_CASE_ID = UUID.fromString("7bc5d41b-0549-40b3-ba76-42f6d4cf3fdb");
  private static final UUID ACTION_ID_3 = UUID.fromString("d24b3f17-bbf8-4c71-b2f0-a4334125d78c");
  private static final UUID ACTION_ID_3_CASE_ID = UUID.fromString("7bc5d41b-0549-40b3-ba76-42f6d4cf3fdc");
  private static final UUID ACTION_ID_4 = UUID.fromString("d24b3f17-bbf8-4c71-b2f0-a4334125d78d");
  private static final UUID ACTION_ID_4_CASE_ID = UUID.fromString("7bc5d41b-0549-40b3-ba76-42f6d4cf3fdd");
  private static final UUID ACTION_ID_5 = UUID.fromString("d24b3f17-bbf8-4c71-b2f0-a4334125d78e");
  private static final UUID ACTION_ID_5_CASE_ID = UUID.fromString("7bc5d41b-0549-40b3-ba76-42f6d4cf3fde");
  private static final UUID ACTION_PLAN_ID_1 = UUID.fromString("5381731e-e386-41a1-8462-26373744db81");
  private static final UUID ACTION_ID_6 = UUID.fromString("d24b3f17-bbf8-4c71-b2f0-a4334125d78f");
  private static final UUID ACTION_ID_7 = UUID.fromString("d24b3f17-bbf8-4c71-b2f0-a4334125d78a");
  private static final UUID ACTION_ID_6_AND_7_CASEID = UUID.fromString("E39202CE-D9A2-4BDD-92F9-E5E0852AF023");
  private static final UUID ACTIONID_1 = UUID.fromString("774afa97-8c87-4131-923b-b33ccbf72b3e");
  private static final UUID ACTIONID_2 = UUID.fromString("64970e28-2ffc-4948-a643-2eb1b42b3fd7");
  private static final UUID ACTION2_PLAN_UUID = UUID.fromString("64970e28-2ffc-4948-a643-2eb1b42b3fd8");

  private static final Boolean ACTION2_ACTIONTYPECANCEL = false;
  private static final Boolean ACTION2_RESPONSEREQUIRED = false;
  private static final Boolean ACTION1_MANUALLY_CREATED = true;
  private static final Boolean ACTION2_MANUALLY_CREATED = false;

  private static final Timestamp ACTION_CREATEDDATE_TIMESTAMP = Timestamp.valueOf("2016-02-26 18:30:00");
  private static final Timestamp ACTION_UPDATEDDATE_TIMESTAMP = Timestamp.valueOf("2016-02-26 19:30:00");

  private static final String ACTION_ACTIONTYPENAME_1 = "action type one";
  private static final String ACTION_ACTIONTYPENAME_2 = "action type two";
  private static final String ACTION_ACTIONTYPENAME_3 = "action type three";
  private static final String ACTION_ACTIONTYPENAME_4 = "action type four";
  private static final String ACTION_ACTIONTYPENAME_5 = "action type five";
  private static final String ACTION_ACTIONTYPENAME_6 = "action type six";
  private static final String ACTION_ACTIONTYPENAME_7 = "action type seven";
  private static final String ACTION_SITUATION_1 = "situation one";
  private static final String ACTION_SITUATION_2 = "situation two";
  private static final String ACTION_SITUATION_3 = "situation three";
  private static final String ACTION_SITUATION_4 = "situation four";
  private static final String ACTION_SITUATION_5 = "situation five";
  private static final String ACTION_SITUATION_6 = "situation six";
  private static final String ACTION_SITUATION_7 = "situation seven";
  private static final String ACTION2_ACTIONTYPENAME = "actiontypename2";
  private static final String ACTION2_ACTIONTYPEDESC = "actiontypedesc2";
  private static final String ACTION2_ACTIONTYPEHANDLER = "Field";
  private static final String ACTION1_SITUATION = "Assigned";
  private static final String ACTION2_SITUATION = "Sent";
  private static final String ACTION_CREATEDBY = "Unit Tester";
  private static final String ALL_ACTIONS_CREATEDDATE_VALUE = "2017-05-15T11:00:00.000+0100";
  private static final String ALL_ACTIONS_UPDATEDDATE_VALUE = "2017-05-15T12:00:00.000+0100";
  private static final String ACTION_CREATEDDATE_VALUE = "2016-02-26T18:30:00.000+0000";
  private static final String ACTION_TYPE_NOTFOUND = "NotFound";
  private static final String OUR_EXCEPTION_MESSAGE = "this is what we throw";

  private static final String ACTION_VALIDJSON = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_ID_6_AND_7_CASEID + "\","
          + "\"actionTypeName\": \"" + ACTION2_ACTIONTYPENAME + "\","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"manuallyCreated\": \"" + ACTION1_MANUALLY_CREATED + "\","
          + "\"priority\": " + ACTION2_PRIORITY + ","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"situation\": \"" + ACTION1_SITUATION + "\","
          + "\"state\": \"" + ACTION2_ACTIONSTATE + "\"}";


  private ActionDTO.ActionState state;

  private Date createdDateTime;

  private Date updatedDateTime;


  private static final String ACTION_INVALIDJSON_PROP = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_ID_6_AND_7_CASEID + "\","
          + "\"actionTypename\": \"" + ACTION2_ACTIONTYPENAME + "\","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"manuallyCreated\": \"" + ACTION1_MANUALLY_CREATED + "\","
          + "\"priority\": " + ACTION2_PRIORITY + ","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"situation\": \"" + ACTION1_SITUATION + "\","
          + "\"state\": \"" + ACTION2_ACTIONSTATE + "\"}";

  private static final String ACTION_INVALIDJSON_MISSING_PROP = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_ID_6_AND_7_CASEID + "\","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"manuallyCreated\": \"" + ACTION1_MANUALLY_CREATED + "\","
          + "\"priority\": " + ACTION2_PRIORITY + ","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"situation\": \"" + ACTION1_SITUATION + "\","
          + "\"state\": \"" + ACTION2_ACTIONSTATE + "\"}";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders
            .standaloneSetup(actionEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();

    actions = FixtureHelper.loadClassFixtures(Action[].class);
    actionPlans = FixtureHelper.loadClassFixtures(ActionPlan[].class);
  }

  /**
   * Test requesting Actions but none found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsNoneFound() throws Exception {
    when(actionService.findAllActionsOrderedByCreatedDateTimeDescending()).thenReturn(new ArrayList<>());

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions")));

    actions.andExpect(status().isNoContent())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"));
  }

  /**
   * Test requesting Actions and returning all the ones found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActions() throws Exception {
    List<Action> results = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      results.add((actions.get(i)));
    }
    when(actionService.findAllActionsOrderedByCreatedDateTimeDescending()).thenReturn(results);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions")));

    actions.andExpect(status().is2xxSuccessful())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(5)))
            .andExpect(jsonPath("$[0].*", hasSize(12)))
            .andExpect(jsonPath("$[*].*", hasSize(60)))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(ACTION_ID_1.toString(), ACTION_ID_2.toString(),
                    ACTION_ID_3.toString(), ACTION_ID_4.toString(), ACTION_ID_5.toString())))
            .andExpect(jsonPath("$[*].caseId", containsInAnyOrder(ACTION_ID_1_CASE_ID.toString(),
                    ACTION_ID_2_CASE_ID.toString(), ACTION_ID_3_CASE_ID.toString(), ACTION_ID_4_CASE_ID.toString(),
                    ACTION_ID_5_CASE_ID.toString())))
            .andExpect(jsonPath("$[*].actionPlanId", containsInAnyOrder(ACTION_PLAN_ID_1.toString(),
                    ACTION_PLAN_ID_1.toString(), ACTION_PLAN_ID_1.toString(), ACTION_PLAN_ID_1.toString(),
                    ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$[*].actionTypeName", containsInAnyOrder(ACTION_ACTIONTYPENAME_1,
                    ACTION_ACTIONTYPENAME_2, ACTION_ACTIONTYPENAME_3, ACTION_ACTIONTYPENAME_4,
                    ACTION_ACTIONTYPENAME_5)))
            .andExpect(jsonPath("$[*].createdBy", containsInAnyOrder(CREATED_BY_SYSTEM, CREATED_BY_SYSTEM,
                    CREATED_BY_SYSTEM, CREATED_BY_SYSTEM, CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$[*].manuallyCreated", containsInAnyOrder(false, false, false, false, false)))
            .andExpect(jsonPath("$[*].priority", containsInAnyOrder(1, 2, 3, 4, 5)))
            .andExpect(jsonPath("$[*].situation", containsInAnyOrder(ACTION_SITUATION_1, ACTION_SITUATION_2,
                    ACTION_SITUATION_3, ACTION_SITUATION_4, ACTION_SITUATION_5)))
            .andExpect(jsonPath("$[*].state", containsInAnyOrder(ActionDTO.ActionState.ACTIVE.name(),
                    ActionDTO.ActionState.SUBMITTED.name(), ActionDTO.ActionState.COMPLETED.name(),
                    ActionDTO.ActionState.CANCELLED.name(), ActionDTO.ActionState.ABORTED.name())))
            .andExpect(jsonPath("$[*].createdDateTime", containsInAnyOrder(ALL_ACTIONS_CREATEDDATE_VALUE,
                    ALL_ACTIONS_CREATEDDATE_VALUE, ALL_ACTIONS_CREATEDDATE_VALUE,
                    ALL_ACTIONS_CREATEDDATE_VALUE, ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[*].updatedDateTime", containsInAnyOrder(ALL_ACTIONS_UPDATEDDATE_VALUE,
                    ALL_ACTIONS_UPDATEDDATE_VALUE, ALL_ACTIONS_UPDATEDDATE_VALUE,
                    ALL_ACTIONS_UPDATEDDATE_VALUE, ALL_ACTIONS_UPDATEDDATE_VALUE)))
    ;
  }

  /**
   * Test requesting Actions filtered by action type name and state not found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByActionTypeAndStateNotFound() throws Exception {
    when(actionService.findActionsByTypeAndStateOrderedByCreatedDateTimeDescending(ACTION_TYPE_NOTFOUND,
            ACTION2_ACTIONSTATE)).thenReturn(new ArrayList<>());

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s&state=%s", ACTION_TYPE_NOTFOUND,
            ACTION2_ACTIONSTATE)));

    actions.andExpect(status().isNoContent())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"));
  }

  /**
   * Test requesting Actions filtered by action type name and state found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByActionTypeAndStateFound() throws Exception {
    List<Action> result = new ArrayList<Action>();
    result.add(actions.get(0));
    when(actionService.findActionsByTypeAndStateOrderedByCreatedDateTimeDescending(ACTION2_ACTIONTYPENAME,
            ACTION2_ACTIONSTATE)).thenReturn(result);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s&state=%s",
            ACTION2_ACTIONTYPENAME, ACTION2_ACTIONSTATE)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].*", hasSize(12)))
            .andExpect(jsonPath("$[0].id", is(ACTION_ID_1.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_ID_1_CASE_ID.toString())))
            .andExpect(jsonPath("$[0].actionPlanId", is(ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$[0].actionTypeName", is(ACTION_ACTIONTYPENAME_1)))
            .andExpect(jsonPath("$[0].createdBy", is(CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$[0].manuallyCreated", is(false)))
            .andExpect(jsonPath("$[0].priority", is(1)))
            .andExpect(jsonPath("$[0].situation", is(ACTION_SITUATION_1)))
            .andExpect(jsonPath("$[0].state", is(ActionDTO.ActionState.ACTIVE.name())))
            .andExpect(jsonPath("$[0].createdDateTime", is(ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[0].updatedDateTime", is(ALL_ACTIONS_UPDATEDDATE_VALUE)));
  }

  /**
   * Test requesting Actions filtered by action type name found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByActionTypeFound() throws Exception {
    List<Action> result = new ArrayList<Action>();
    result.add(actions.get(0));
    when(actionService.findActionsByType(ACTION2_ACTIONTYPENAME)).thenReturn(result);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s", ACTION2_ACTIONTYPENAME)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].*", hasSize(12)))
            .andExpect(jsonPath("$[0].id", is(ACTION_ID_1.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_ID_1_CASE_ID.toString())))
            .andExpect(jsonPath("$[0].actionPlanId", is(ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$[0].actionTypeName", is(ACTION_ACTIONTYPENAME_1)))
            .andExpect(jsonPath("$[0].createdBy", is(CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$[0].manuallyCreated", is(false)))
            .andExpect(jsonPath("$[0].priority", is(1)))
            .andExpect(jsonPath("$[0].situation", is(ACTION_SITUATION_1)))
            .andExpect(jsonPath("$[0].state", is(ActionDTO.ActionState.ACTIVE.name())))
            .andExpect(jsonPath("$[0].createdDateTime", is(ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[0].updatedDateTime", is(ALL_ACTIONS_UPDATEDDATE_VALUE)));
  }


  /**
   * Test requesting Actions filtered by action type name not found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByActionTypeNotFound() throws Exception {
    when(actionService.findActionsByType(ACTION_TYPE_NOTFOUND)).thenReturn(new ArrayList<Action>());

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s", ACTION_TYPE_NOTFOUND)));

    actions.andExpect(status().isNoContent())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"));
  }

  /**
   * Test requesting Actions filtered by action state found.
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByStateFound() throws Exception {
    List<Action> result = new ArrayList<Action>();
    result.add(actions.get(0));
    when(actionService.findActionsByState(ACTION2_ACTIONSTATE)).thenReturn(result);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?state=%s", ACTION2_ACTIONSTATE.toString())));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].*", hasSize(12)))
            .andExpect(jsonPath("$[0].id", is(ACTION_ID_1.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_ID_1_CASE_ID.toString())))
            .andExpect(jsonPath("$[0].actionPlanId", is(ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$[0].actionTypeName", is(ACTION_ACTIONTYPENAME_1)))
            .andExpect(jsonPath("$[0].createdBy", is(CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$[0].manuallyCreated", is(false)))
            .andExpect(jsonPath("$[0].priority", is(1)))
            .andExpect(jsonPath("$[0].situation", is(ACTION_SITUATION_1)))
            .andExpect(jsonPath("$[0].state", is(ActionDTO.ActionState.ACTIVE.name())))
            .andExpect(jsonPath("$[0].createdDateTime", is(ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[0].updatedDateTime", is(ALL_ACTIONS_UPDATEDDATE_VALUE)));
  }

  /**
   * Test requesting an Action by action Id not found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionByActionIdNotFound() throws Exception {
    ResultActions actions = mockMvc.perform(getJson(String.format("/actions/%s", NON_EXISTING_ID)));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionByActionId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", is(String.format("Action not found for id %s",
                    NON_EXISTING_ID))))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * Test requesting Actions by case Id found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByCaseIdFound() throws Exception {
    List<Action> result = new ArrayList<Action>();
    result.add(actions.get(5));
    result.add(actions.get(6));
    when(actionService.findActionsByCaseId(ACTION_ID_6_AND_7_CASEID)).thenReturn(result);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions/case/%s", ACTION_ID_6_AND_7_CASEID)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionsByCaseId"))
            .andExpect(jsonPath("$", Matchers.hasSize(2)))
            .andExpect(jsonPath("$[0].*", hasSize(12)))
            .andExpect(jsonPath("$[1].*", hasSize(12)))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(ACTION_ID_6.toString(), ACTION_ID_7.toString())))
            .andExpect(jsonPath("$[*].caseId", containsInAnyOrder(ACTION_ID_6_AND_7_CASEID.toString(),
                    ACTION_ID_6_AND_7_CASEID.toString())))
            .andExpect(jsonPath("$[*].actionPlanId", containsInAnyOrder(ACTION_PLAN_ID_1.toString(),
                    ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$[*].actionTypeName", containsInAnyOrder(ACTION_ACTIONTYPENAME_6,
                    ACTION_ACTIONTYPENAME_7)))
            .andExpect(jsonPath("$[*].createdBy", containsInAnyOrder(CREATED_BY_SYSTEM, CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$[*].manuallyCreated", containsInAnyOrder(false, true)))
            .andExpect(jsonPath("$[*].priority", containsInAnyOrder(6, 7)))
            .andExpect(jsonPath("$[*].situation", containsInAnyOrder(ACTION_SITUATION_6, ACTION_SITUATION_7)))
            .andExpect(jsonPath("$[*].state", containsInAnyOrder(ActionDTO.ActionState.ABORTED.name(),
                    ActionDTO.ActionState.CANCELLED.name())))
            .andExpect(jsonPath("$[*].createdDateTime", containsInAnyOrder(ALL_ACTIONS_CREATEDDATE_VALUE,
                    ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[*].updatedDateTime", containsInAnyOrder(ALL_ACTIONS_UPDATEDDATE_VALUE,
                    ALL_ACTIONS_UPDATEDDATE_VALUE)));
  }

  /**
   * Test requesting Actions by case Id not found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionByCaseIdNotFound() throws Exception {
    ResultActions actions = mockMvc.perform(getJson(String.format("/actions/case/%s", NON_EXISTING_ID)));

    actions.andExpect(status().isNoContent())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionsByCaseId"));
  }

  /**
   * Test updating action not found
   * @throws Exception when putJson does
   */
  @Test
  public void updateActionByActionIdNotFound() throws Exception {
    ResultActions actions = mockMvc.perform(putJson(String.format("/actions/%s", NON_EXISTING_ID), ACTION_VALIDJSON));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("updateAction"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())));
  }

  /**
   * Test requesting an Action creating an Unchecked Exception.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionByActionIdUnCheckedException() throws Exception {
    when(actionService.findActionById(ACTIONID_1)).thenThrow(new IllegalArgumentException(OUR_EXCEPTION_MESSAGE));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions/%s", ACTIONID_1)));

    actions.andExpect(status().is5xxServerError())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionByActionId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.SYSTEM_ERROR.name())))
            .andExpect(jsonPath("$.error.message", is(OUR_EXCEPTION_MESSAGE)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * Test creating an Action with valid JSON.
   * @throws Exception when postJson does
   */
  @Test
  public void createActionGoodJsonProvided() throws Exception {
    ActionType actionType = new ActionType(1, ACTION2_ACTIONTYPENAME, ACTION2_ACTIONTYPEDESC,
            ACTION2_ACTIONTYPEHANDLER, ACTION2_ACTIONTYPECANCEL, ACTION2_RESPONSEREQUIRED);
    Action action = new Action(ACTION_PK, ACTIONID_2, ACTION_ID_6_AND_7_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION2_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0);
    when(actionService.createAction(any(Action.class))).thenReturn(action);

    ResultActions actions = mockMvc.perform(postJson("/actions", ACTION_VALIDJSON));

    actions.andExpect(status().isCreated())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("createAction"))
            .andExpect(jsonPath("$.id", is(ACTIONID_2.toString())))
            .andExpect(jsonPath("$.caseId", is(ACTION_ID_6_AND_7_CASEID.toString())))
            .andExpect(jsonPath("$.actionTypeName", is(ACTION2_ACTIONTYPENAME)))
            .andExpect(jsonPath("$.createdBy", is(ACTION_CREATEDBY)))
            .andExpect(jsonPath("$.priority", is(ACTION2_PRIORITY)))
            .andExpect(jsonPath("$.situation", is(ACTION2_SITUATION)))
            .andExpect(jsonPath("$.state", is(ACTION2_ACTIONSTATE.name())))
            .andExpect(jsonPath("$.createdDateTime", is(ACTION_CREATEDDATE_VALUE)));
  }

  /**
   * Test creating an Action with invalid JSON Property.
   * @throws Exception when postJson does
   */
  @Test
  public void createActionInvalidPropJsonProvided() throws Exception {
    ResultActions actions = mockMvc.perform(postJson("/actions", ACTION_INVALIDJSON_PROP));

    actions.andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("createAction"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
            .andExpect(jsonPath("$.error.message", is(PROVIDED_JSON_INCORRECT)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }


  /**
   * Test creating an Action with missing JSON Property.
   * @throws Exception when postJson does
   */
  @Test
  public void createActionMissingPropJsonProvided() throws Exception {
    ResultActions actions = mockMvc.perform(postJson("/actions", ACTION_INVALIDJSON_MISSING_PROP));

    actions.andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("createAction"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
            .andExpect(jsonPath("$.error.message", is(INVALID_JSON)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * Test cancelling an Action.
   * @throws Exception when putJson does
   */
  @Test
  public void cancelActions() throws Exception {
    when(actionCaseService.findActionCase(ACTION_ID_6_AND_7_CASEID)).thenReturn(new ActionCase());


    ActionType actionType = new ActionType(1, ACTION2_ACTIONTYPENAME, ACTION2_ACTIONTYPEDESC,
            ACTION2_ACTIONTYPEHANDLER, ACTION2_ACTIONTYPECANCEL, ACTION2_RESPONSEREQUIRED);

    Action action = new Action(ACTION_PK, ACTIONID_2, ACTION_ID_6_AND_7_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION3_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0);
    List<Action> result = new ArrayList<>();
    result.add(action);
    when(actionService.cancelActions(ACTION_ID_6_AND_7_CASEID)).thenReturn(result);


    ResultActions actions = mockMvc.perform(putJson(String.format("/actions/case/%s/cancel", ACTION_ID_6_AND_7_CASEID),
            ""));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("cancelActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(ACTIONID_2.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_ID_6_AND_7_CASEID.toString())))
            .andExpect(jsonPath("$[0].actionTypeName", is(ACTION2_ACTIONTYPENAME)))
            .andExpect(jsonPath("$[0].createdBy", is(ACTION_CREATEDBY)))
            .andExpect(jsonPath("$[0].priority", is(ACTION2_PRIORITY)))
            .andExpect(jsonPath("$[0].situation", is(ACTION2_SITUATION)))
            .andExpect(jsonPath("$[0].state", is(ACTION3_ACTIONSTATE.name())))
            .andExpect(jsonPath("$[0].createdDateTime", is(ACTION_CREATEDDATE_VALUE)));
  }

  /**
   * Test cancelling an Action.
   * @throws Exception when putJson does
   */
  @Test
  public void cancelActionsCaseNotFound() throws Exception {
    ResultActions actions = mockMvc.perform(putJson(String.format("/actions/case/%s/cancel", NON_EXISTING_ID),
            ""));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("cancelActions"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())));
  }
}
