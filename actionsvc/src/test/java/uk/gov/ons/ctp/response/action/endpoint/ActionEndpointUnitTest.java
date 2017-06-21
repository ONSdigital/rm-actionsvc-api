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
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.service.ActionCaseService;
import uk.gov.ons.ctp.response.action.service.ActionPlanService;
import uk.gov.ons.ctp.response.action.service.ActionService;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import static uk.gov.ons.ctp.response.action.endpoint.ActionEndpoint.ACTION_NOT_FOUND;
import static uk.gov.ons.ctp.response.action.endpoint.ActionEndpoint.ACTION_NOT_UPDATED;
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

  private static final Boolean ACTION1_MANUALLY_CREATED = true;

  private static final Integer ACTION1_PRIORITY = 1;

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
  private static final String ACTION1_SITUATION = "Assigned";
  private static final String ACTION_CREATEDBY = "Unit Tester";
  private static final String ALL_ACTIONS_CREATEDDATE_VALUE = "2017-05-15T11:00:00.000+0100";
  private static final String ALL_ACTIONS_UPDATEDDATE_VALUE = "2017-05-15T12:00:00.000+0100";
  private static final String ACTION_TYPE_NOTFOUND = "NotFound";
  private static final String NON_EXISTING_ID = "e1c26bf2-eaa8-4a8a-b44f-3b8f004ef271";
  private static final String OUR_EXCEPTION_MESSAGE = "this is what we throw";
  private static final String UPDATED_OUTCOME = "REQUEST_COMPLETED";
  private static final String UPDATED_SITUATION =  "new situation";

  private static final String ACTION_VALID_JSON = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_ID_1_CASE_ID + "\","
          + "\"actionTypeName\": \"" + ACTION_ACTIONTYPENAME_1 + "\","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"manuallyCreated\": \"" + ACTION1_MANUALLY_CREATED + "\","
          + "\"priority\": " + ACTION1_PRIORITY + ","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"situation\": \"" + ACTION1_SITUATION + "\","
          + "\"state\": \"" + ActionDTO.ActionState.ACTIVE.name() + "\"}";

  // Note actionTypename instead of actionTypeName
  private static final String ACTION_INVALID_JSON_BAD_PROP = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_ID_1_CASE_ID + "\","
          + "\"actionTypename\": \"" + ACTION_ACTIONTYPENAME_1 + "\","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"manuallyCreated\": \"" + ACTION1_MANUALLY_CREATED + "\","
          + "\"priority\": " + ACTION1_PRIORITY + ","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"situation\": \"" + ACTION1_SITUATION + "\","
          + "\"state\": \"" + ActionDTO.ActionState.ACTIVE.name() + "\"}";

  // Note actionTypeName is missing
  private static final String ACTION_INVALID_JSON_MISSING_PROP = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_ID_1_CASE_ID + "\","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"manuallyCreated\": \"" + ACTION1_MANUALLY_CREATED + "\","
          + "\"priority\": " + ACTION1_PRIORITY + ","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"situation\": \"" + ACTION1_SITUATION + "\","
          + "\"state\": \"" + ActionDTO.ActionState.ACTIVE.name() + "\"}";

  private static final String ACTION_FEEDBACK_VALID_JSON = "{"
          + "\"situation\": \"" + UPDATED_SITUATION + "\","
          + "\"outcome\": \"" + UPDATED_OUTCOME + "\"}";

  private static final String ACTION_FEEDBACK_INVALID_JSON = "{"
          + "\"badsituation\": \"" + UPDATED_SITUATION + "\","
          + "\"outcome\": \"" + UPDATED_OUTCOME + "\"}";

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

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions")));

    resultActions.andExpect(status().isNoContent())
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

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions")));

    resultActions.andExpect(status().is2xxSuccessful())
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
            ActionDTO.ActionState.COMPLETED)).thenReturn(new ArrayList<>());

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s&state=%s", ACTION_TYPE_NOTFOUND,
            ActionDTO.ActionState.COMPLETED)));

    resultActions.andExpect(status().isNoContent())
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
            ActionDTO.ActionState.COMPLETED)).thenReturn(result);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s&state=%s",
            ACTION2_ACTIONTYPENAME, ActionDTO.ActionState.COMPLETED)));

    resultActions.andExpect(status().isOk())
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

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s",
            ACTION2_ACTIONTYPENAME)));

    resultActions.andExpect(status().isOk())
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

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s",
            ACTION_TYPE_NOTFOUND)));

    resultActions.andExpect(status().isNoContent())
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
    when(actionService.findActionsByState(ActionDTO.ActionState.COMPLETED)).thenReturn(result);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions?state=%s", ActionDTO.ActionState.COMPLETED.name())));

    resultActions.andExpect(status().isOk())
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
    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions/%s", NON_EXISTING_ID)));

    resultActions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionByActionId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", is(String.format(ACTION_NOT_FOUND, NON_EXISTING_ID))))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }


  /**
   * Test requesting an Action creating an Unchecked Exception.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionByActionIdUnCheckedException() throws Exception {
    when(actionService.findActionById(ACTIONID_1)).thenThrow(new IllegalArgumentException(OUR_EXCEPTION_MESSAGE));

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions/%s", ACTIONID_1)));

    resultActions.andExpect(status().is5xxServerError())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionByActionId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.SYSTEM_ERROR.name())))
            .andExpect(jsonPath("$.error.message", is(OUR_EXCEPTION_MESSAGE)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * Test requesting an Action by action Id.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionByActionId() throws Exception {
    when(actionService.findActionById(ACTION_ID_1)).thenReturn(actions.get(0));
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions/%s", ACTION_ID_1)));

    resultActions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionByActionId"))
            .andExpect(jsonPath("$.*", Matchers.hasSize(12)))
            .andExpect(jsonPath("$.id", is(ACTION_ID_1.toString())))
            .andExpect(jsonPath("$.caseId", is(ACTION_ID_1_CASE_ID.toString())))
            .andExpect(jsonPath("$.actionPlanId", is(ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$.actionTypeName", is(ACTION_ACTIONTYPENAME_1)))
            .andExpect(jsonPath("$.createdBy", is(CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$.manuallyCreated", is(false)))
            .andExpect(jsonPath("$.priority", is(1)))
            .andExpect(jsonPath("$.situation", is(ACTION_SITUATION_1)))
            .andExpect(jsonPath("$.state", is(ActionDTO.ActionState.ACTIVE.name())))
            .andExpect(jsonPath("$.createdDateTime", is(ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$.updatedDateTime", is(ALL_ACTIONS_UPDATEDDATE_VALUE)));
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

    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions/case/%s", ACTION_ID_6_AND_7_CASEID)));

    resultActions.andExpect(status().isOk())
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
    ResultActions resultActions = mockMvc.perform(getJson(String.format("/actions/case/%s", NON_EXISTING_ID)));

    resultActions.andExpect(status().isNoContent())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionsByCaseId"));
  }

  /**
   * Test updating action not found
   * @throws Exception when putJson does
   */
  @Test
  public void updateActionByActionIdNotFound() throws Exception {
    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/%s", NON_EXISTING_ID),
            ACTION_VALID_JSON));

    resultActions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("updateAction"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", is(String.format(ACTION_NOT_UPDATED, NON_EXISTING_ID))))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
    ;
  }

  /**
   * Test updating action
   * @throws Exception when putJson does
   */
  @Test
  public void updateActionByActionId() throws Exception {
    when(actionService.updateAction(any(Action.class))).thenReturn(actions.get(0));
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/%s", ACTION_ID_1),
            ACTION_VALID_JSON));

    resultActions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("updateAction"))
            .andExpect(jsonPath("$.*", Matchers.hasSize(12)))
            .andExpect(jsonPath("$.id", is(ACTION_ID_1.toString())))
            .andExpect(jsonPath("$.caseId", is(ACTION_ID_1_CASE_ID.toString())))
            .andExpect(jsonPath("$.actionPlanId", is(ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$.actionTypeName", is(ACTION_ACTIONTYPENAME_1)))
            .andExpect(jsonPath("$.createdBy", is(CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$.manuallyCreated", is(false)))
            .andExpect(jsonPath("$.priority", is(1)))
            .andExpect(jsonPath("$.situation", is(ACTION_SITUATION_1)))
            .andExpect(jsonPath("$.state", is(ActionDTO.ActionState.ACTIVE.name())))
            .andExpect(jsonPath("$.createdDateTime", is(ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$.updatedDateTime", is(ALL_ACTIONS_UPDATEDDATE_VALUE)));
  }

  /**
   * Test updating action with invalid json
   * @throws Exception when putJson does
   */
  @Test
  public void updateActionByActionIdWithInvalidJson() throws Exception {
    when(actionService.updateAction(any(Action.class))).thenReturn(actions.get(0));
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/%s", ACTION_ID_1),
            ACTION_INVALID_JSON_BAD_PROP));

    resultActions.andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("updateAction"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
            .andExpect(jsonPath("$.error.message", is(PROVIDED_JSON_INCORRECT)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * Test updating action feedback for action not found
   * @throws Exception when putJson does
   */
  @Test
  public void updateActionFeedbackByActionIdNotFound() throws Exception {
    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/%s/feedback", NON_EXISTING_ID),
            ACTION_FEEDBACK_VALID_JSON));

    resultActions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("feedbackAction"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", is(String.format(ACTION_NOT_FOUND, NON_EXISTING_ID))))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * Test updating action feedback for action found BUT bad json
   * @throws Exception when putJson does
   */
  @Test
  public void updateActionFeedbackByActionIdFoundButBadJson() throws Exception {
    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/%s/feedback", ACTION_ID_1),
            ACTION_FEEDBACK_INVALID_JSON));

    resultActions.andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("feedbackAction"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
            .andExpect(jsonPath("$.error.message", is(PROVIDED_JSON_INCORRECT)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * Test updating action feedback for action found
   * @throws Exception when putJson does
   */
  @Test
  public void updateActionFeedbackByActionIdFound() throws Exception {
    when(actionService.feedBackAction(any(ActionFeedback.class))).thenReturn(actions.get(0));
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/%s/feedback", ACTION_ID_1),
            ACTION_FEEDBACK_VALID_JSON));

    resultActions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("feedbackAction"))
            .andExpect(jsonPath("$.*", Matchers.hasSize(12)))
            .andExpect(jsonPath("$.id", is(ACTION_ID_1.toString())))
            .andExpect(jsonPath("$.caseId", is(ACTION_ID_1_CASE_ID.toString())))
            .andExpect(jsonPath("$.actionPlanId", is(ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$.actionTypeName", is(ACTION_ACTIONTYPENAME_1)))
            .andExpect(jsonPath("$.createdBy", is(CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$.manuallyCreated", is(false)))
            .andExpect(jsonPath("$.priority", is(1)))
            .andExpect(jsonPath("$.situation", is(ACTION_SITUATION_1)))
            .andExpect(jsonPath("$.state", is(ActionDTO.ActionState.ACTIVE.name())))
            .andExpect(jsonPath("$.createdDateTime", is(ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$.updatedDateTime", is(ALL_ACTIONS_UPDATEDDATE_VALUE)));
  }

  /**
   * Test creating an Action with valid JSON.
   * @throws Exception when postJson does
   */
  @Test
  public void createActionGoodJsonProvided() throws Exception {
    when(actionService.createAction(any(Action.class))).thenReturn(actions.get(0));
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(postJson("/actions", ACTION_VALID_JSON));

    resultActions.andExpect(status().isCreated())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("createAction"))
            .andExpect(jsonPath("$.*", Matchers.hasSize(12)))
            .andExpect(jsonPath("$.id", is(ACTION_ID_1.toString())))
            .andExpect(jsonPath("$.caseId", is(ACTION_ID_1_CASE_ID.toString())))
            .andExpect(jsonPath("$.actionPlanId", is(ACTION_PLAN_ID_1.toString())))
            .andExpect(jsonPath("$.actionTypeName", is(ACTION_ACTIONTYPENAME_1)))
            .andExpect(jsonPath("$.createdBy", is(CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$.manuallyCreated", is(false)))
            .andExpect(jsonPath("$.priority", is(1)))
            .andExpect(jsonPath("$.situation", is(ACTION_SITUATION_1)))
            .andExpect(jsonPath("$.state", is(ActionDTO.ActionState.ACTIVE.name())))
            .andExpect(jsonPath("$.createdDateTime", is(ALL_ACTIONS_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$.updatedDateTime", is(ALL_ACTIONS_UPDATEDDATE_VALUE)));
  }

  /**
   * Test creating an Action with invalid JSON Property.
   * @throws Exception when postJson does
   */
  @Test
  public void createActionInvalidPropJsonProvided() throws Exception {
    ResultActions resultActions = mockMvc.perform(postJson("/actions", ACTION_INVALID_JSON_BAD_PROP));

    resultActions.andExpect(status().isBadRequest())
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
    ResultActions resultActions = mockMvc.perform(postJson("/actions", ACTION_INVALID_JSON_MISSING_PROP));

    resultActions.andExpect(status().isBadRequest())
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

    List<Action> result = new ArrayList<>();
    result.add(actions.get(5));
    result.add(actions.get(6));
    when(actionService.cancelActions(ACTION_ID_6_AND_7_CASEID)).thenReturn(result);
    when(actionPlanService.findActionPlan(any(Integer.class))).thenReturn(actionPlans.get(0));

    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/case/%s/cancel",
            ACTION_ID_6_AND_7_CASEID), ""));

    resultActions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("cancelActions"))
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
   * Test cancelling an Action for a Case Not Found.
   * @throws Exception when putJson does
   */
  @Test
  public void cancelActionsCaseNotFound() throws Exception {
    ResultActions resultActions = mockMvc.perform(putJson(String.format("/actions/case/%s/cancel", NON_EXISTING_ID),
            ""));

    resultActions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("cancelActions"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())));
  }
}
