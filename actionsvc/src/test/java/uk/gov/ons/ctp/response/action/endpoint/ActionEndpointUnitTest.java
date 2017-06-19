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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.common.MvcHelper.*;
import static uk.gov.ons.ctp.common.error.RestExceptionHandler.INVALID_JSON;
import static uk.gov.ons.ctp.common.error.RestExceptionHandler.PROVIDED_JSON_INCORRECT;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

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

  private static final ActionDTO.ActionState ACTION1_ACTIONSTATE = ActionDTO.ActionState.ACTIVE;
  private static final ActionDTO.ActionState ACTION2_ACTIONSTATE = ActionDTO.ActionState.COMPLETED;
  private static final ActionDTO.ActionState ACTION3_ACTIONSTATE = ActionDTO.ActionState.CANCELLED;

  private static final Integer ACTION_CASEFK = 1;
  private static final Integer ACTION1_PRIORITY = 1;
  private static final Integer ACTION2_PRIORITY = 3;
  private static final Integer ACTION1_PLAN_FK = 1;
  private static final Integer ACTION2_PLAN_FK = 2;
  private static final Integer ACTION1_RULE_FK = 1;
  private static final Integer ACTION2_RULE_FK = 2;
  private static final String NON_EXISTING_ID = "e1c26bf2-eaa8-4a8a-b44f-3b8f004ef271";

  private static final BigInteger ACTION_PK = BigInteger.valueOf(1);

  private static final UUID ACTION_CASEID = UUID.fromString("E39202CE-D9A2-4BDD-92F9-E5E0852AF023");
  private static final UUID ACTIONID_1 = UUID.fromString("774afa97-8c87-4131-923b-b33ccbf72b3e");
  private static final UUID ACTIONID_2 = UUID.fromString("64970e28-2ffc-4948-a643-2eb1b42b3fd7");
  private static final UUID ACTION2_PLAN_UUID = UUID.fromString("64970e28-2ffc-4948-a643-2eb1b42b3fd8");

  private static final Boolean ACTION1_ACTIONTYPECANCEL = true;
  private static final Boolean ACTION2_ACTIONTYPECANCEL = false;
  private static final Boolean ACTION1_RESPONSEREQUIRED = true;
  private static final Boolean ACTION2_RESPONSEREQUIRED = false;
  private static final Boolean ACTION1_MANUALLY_CREATED = true;
  private static final Boolean ACTION2_MANUALLY_CREATED = false;

  private static final Timestamp ACTION_CREATEDDATE_TIMESTAMP = Timestamp.valueOf("2016-02-26 18:30:00");
  private static final Timestamp ACTION_UPDATEDDATE_TIMESTAMP = Timestamp.valueOf("2016-02-26 19:30:00");

  private static final String ACTION1_ACTIONTYPENAME = "actiontypename1";
  private static final String ACTION2_ACTIONTYPENAME = "actiontypename2";
  private static final String ACTION1_ACTIONTYPEDESC = "actiontypedesc1";
  private static final String ACTION2_ACTIONTYPEDESC = "actiontypedesc2";
  private static final String ACTION1_ACTIONTYPEHANDLER = "Field";
  private static final String ACTION2_ACTIONTYPEHANDLER = "Field";
  private static final String ACTION1_SITUATION = "Assigned";
  private static final String ACTION2_SITUATION = "Sent";
  private static final String ACTION_CREATEDBY = "Unit Tester";
  private static final String ACTION_CREATEDDATE_VALUE = "2016-02-26T18:30:00.000+0000";
  private static final String ACTION_UPDATEDDATE_VALUE = "2016-02-26T19:30:00.000+0000";
  private static final String ACTION_NOTFOUND = "NotFound";
  private static final String OUR_EXCEPTION_MESSAGE = "this is what we throw";

  private static final String ACTION_VALIDJSON = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_CASEID + "\","
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
          + "\"caseId\": \"" + ACTION_CASEID + "\","
          + "\"actionTypename\": \"" + ACTION2_ACTIONTYPENAME + "\","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"manuallyCreated\": \"" + ACTION1_MANUALLY_CREATED + "\","
          + "\"priority\": " + ACTION2_PRIORITY + ","
          + "\"createdBy\": \"" + ACTION_CREATEDBY + "\","
          + "\"situation\": \"" + ACTION1_SITUATION + "\","
          + "\"state\": \"" + ACTION2_ACTIONSTATE + "\"}";

  private static final String ACTION_INVALIDJSON_MISSING_PROP = "{"
          + "\"id\": \"" + ACTIONID_1 + "\","
          + "\"caseId\": \"" + ACTION_CASEID + "\","
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
   * Test requesting Actions filtered by action type name and state not found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByActionTypeAndStateNotFound() throws Exception {
    when(actionService.findActionsByTypeAndStateOrderedByCreatedDateTimeDescending(ACTION_NOTFOUND,
            ACTION2_ACTIONSTATE)).thenReturn(new ArrayList<>());

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s&state=%s", ACTION_NOTFOUND,
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
    ActionType actionType = new ActionType(1, ACTION2_ACTIONTYPENAME, ACTION2_ACTIONTYPEDESC,
            ACTION2_ACTIONTYPEHANDLER, ACTION2_ACTIONTYPECANCEL, ACTION2_RESPONSEREQUIRED);
    result.add(new Action(ACTION_PK, ACTIONID_2, ACTION_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION2_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0));
    when(actionService.findActionsByTypeAndStateOrderedByCreatedDateTimeDescending(ACTION2_ACTIONTYPENAME,
            ACTION2_ACTIONSTATE)).thenReturn(result);

    ActionPlan actionPlan = new ActionPlan();
    actionPlan.setId(ACTION2_PLAN_UUID);
    when(actionPlanService.findActionPlan(ACTION2_PLAN_FK)).thenReturn(actionPlan);

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s&state=%s",
            ACTION2_ACTIONTYPENAME, ACTION2_ACTIONSTATE)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(ACTIONID_2.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_CASEID.toString())))
            .andExpect(jsonPath("$[0].actionPlanId", is(ACTION2_PLAN_UUID.toString())))
// TODO            .andExpect(jsonPath("$[0].actionRuleId", is(ACTION2_PLAN_UUID.toString())))
            .andExpect(jsonPath("$[0].actionTypeName", is(ACTION2_ACTIONTYPENAME)))
            .andExpect(jsonPath("$[0].createdBy", is(ACTION_CREATEDBY)))
            .andExpect(jsonPath("$[0].manuallyCreated", is(ACTION2_MANUALLY_CREATED)))
            .andExpect(jsonPath("$[0].priority", is(ACTION2_PRIORITY)))
            .andExpect(jsonPath("$[0].situation", is(ACTION2_SITUATION)))
            .andExpect(jsonPath("$[0].state", is(ACTION2_ACTIONSTATE.name())))
            .andExpect(jsonPath("$[0].createdDateTime", is(ACTION_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[0].updatedDateTime", is(ACTION_UPDATEDDATE_VALUE)));
  }

  /**
   * Test requesting Actions filtered by action type name found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByActionTypeFound() throws Exception {
    List<Action> result = new ArrayList<Action>();
    ActionType actionType = new ActionType(1, ACTION2_ACTIONTYPENAME, ACTION2_ACTIONTYPEDESC,
            ACTION2_ACTIONTYPEHANDLER, ACTION2_ACTIONTYPECANCEL, ACTION2_RESPONSEREQUIRED);
    result.add(new Action(ACTION_PK, ACTIONID_2, ACTION_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION2_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0));
    when(actionService.findActionsByType(ACTION2_ACTIONTYPENAME)).thenReturn(result);

    ActionPlan actionPlan = new ActionPlan();
    actionPlan.setId(ACTION2_PLAN_UUID);
    when(actionPlanService.findActionPlan(ACTION2_PLAN_FK)).thenReturn(actionPlan);

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s", ACTION2_ACTIONTYPENAME)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(ACTIONID_2.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_CASEID.toString())))
            .andExpect(jsonPath("$[0].actionPlanId", is(ACTION2_PLAN_UUID.toString())))
// TODO            .andExpect(jsonPath("$[0].actionRuleId", is(ACTION2_PLAN_UUID.toString())))
            .andExpect(jsonPath("$[0].actionTypeName", is(ACTION2_ACTIONTYPENAME)))
            .andExpect(jsonPath("$[0].createdBy", is(ACTION_CREATEDBY)))
            .andExpect(jsonPath("$[0].manuallyCreated", is(ACTION2_MANUALLY_CREATED)))
            .andExpect(jsonPath("$[0].priority", is(ACTION2_PRIORITY)))
            .andExpect(jsonPath("$[0].situation", is(ACTION2_SITUATION)))
            .andExpect(jsonPath("$[0].state", is(ACTION2_ACTIONSTATE.name())))
            .andExpect(jsonPath("$[0].createdDateTime", is(ACTION_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[0].updatedDateTime", is(ACTION_UPDATEDDATE_VALUE)));
  }


  /**
   * Test requesting Actions filtered by action type name not found.
   *
   * @throws Exception when getJson does
   */
  @Test
  public void findActionsByActionTypeNotFound() throws Exception {
    when(actionService.findActionsByType(ACTION_NOTFOUND)).thenReturn(new ArrayList<Action>());

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?actiontype=%s", ACTION_NOTFOUND)));

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
    ActionType actionType = new ActionType(1, ACTION2_ACTIONTYPENAME, ACTION2_ACTIONTYPEDESC,
            ACTION2_ACTIONTYPEHANDLER, ACTION2_ACTIONTYPECANCEL, ACTION2_RESPONSEREQUIRED);
    result.add(new Action(ACTION_PK, ACTIONID_2, ACTION_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION2_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0));
    when(actionService.findActionsByState(ACTION2_ACTIONSTATE)).thenReturn(result);

    ActionPlan actionPlan = new ActionPlan();
    actionPlan.setId(ACTION2_PLAN_UUID);
    when(actionPlanService.findActionPlan(ACTION2_PLAN_FK)).thenReturn(actionPlan);

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions?state=%s",
            ACTION2_ACTIONSTATE.toString())));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(ACTIONID_2.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_CASEID.toString())))
            .andExpect(jsonPath("$[0].actionPlanId", is(ACTION2_PLAN_UUID.toString())))
// TODO            .andExpect(jsonPath("$[0].actionRuleId", is(ACTION2_PLAN_UUID.toString())))
            .andExpect(jsonPath("$[0].actionTypeName", is(ACTION2_ACTIONTYPENAME)))
            .andExpect(jsonPath("$[0].createdBy", is(ACTION_CREATEDBY)))
            .andExpect(jsonPath("$[0].manuallyCreated", is(ACTION2_MANUALLY_CREATED)))
            .andExpect(jsonPath("$[0].priority", is(ACTION2_PRIORITY)))
            .andExpect(jsonPath("$[0].situation", is(ACTION2_SITUATION)))
            .andExpect(jsonPath("$[0].state", is(ACTION2_ACTIONSTATE.name())))
            .andExpect(jsonPath("$[0].createdDateTime", is(ACTION_CREATEDDATE_VALUE)))
            .andExpect(jsonPath("$[0].updatedDateTime", is(ACTION_UPDATEDDATE_VALUE)));
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
    ActionType actionType1 = new ActionType(1, ACTION1_ACTIONTYPENAME, ACTION1_ACTIONTYPEDESC,
            ACTION1_ACTIONTYPEHANDLER, ACTION1_ACTIONTYPECANCEL, ACTION1_RESPONSEREQUIRED);
    ActionType actionType2 = new ActionType(1, ACTION2_ACTIONTYPENAME, ACTION2_ACTIONTYPEDESC,
            ACTION2_ACTIONTYPEHANDLER, ACTION2_ACTIONTYPECANCEL, ACTION2_RESPONSEREQUIRED);
    result.add(new Action(ACTION_PK, ACTIONID_1, ACTION_CASEID, ACTION_CASEFK, ACTION1_PLAN_FK, ACTION1_RULE_FK,
            ACTION_CREATEDBY, ACTION1_MANUALLY_CREATED, actionType1, ACTION1_PRIORITY, ACTION1_SITUATION,
            ACTION1_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0));
    result.add(new Action(ACTION_PK, ACTIONID_2, ACTION_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType2, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION2_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0));
    when(actionService.findActionsByCaseId(ACTION_CASEID)).thenReturn(result);

    System.out.println(result.get(0).toString());
    System.out.println(result.get(1).toString());

    ResultActions actions = mockMvc.perform(getJson(String.format("/actions/case/%s", ACTION_CASEID)));

    System.out.println(actions.andReturn().getResponse().getContentAsString());

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("findActionsByCaseId"))
            .andExpect(jsonPath("$", Matchers.hasSize(2)))
            .andExpect(jsonPath("$[*].caseId", containsInAnyOrder(ACTION_CASEID.toString(),
                    ACTION_CASEID.toString())))
            .andExpect(jsonPath("$[*].actionTypeName", containsInAnyOrder(ACTION1_ACTIONTYPENAME,
                    ACTION2_ACTIONTYPENAME)))
            .andExpect(jsonPath("$[*].createdBy", containsInAnyOrder(ACTION_CREATEDBY, ACTION_CREATEDBY)))
            .andExpect(jsonPath("$[*].priority", containsInAnyOrder(ACTION1_PRIORITY, ACTION2_PRIORITY)))
            .andExpect(jsonPath("$[*].situation", containsInAnyOrder(ACTION1_SITUATION, ACTION2_SITUATION)))
            .andExpect(jsonPath("$[*].state", containsInAnyOrder(ACTION1_ACTIONSTATE.name(),
                    ACTION2_ACTIONSTATE.name())))
            .andExpect(jsonPath("$[*].createdDateTime", containsInAnyOrder(ACTION_CREATEDDATE_VALUE,
                    ACTION_CREATEDDATE_VALUE)));
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
    Action action = new Action(ACTION_PK, ACTIONID_2, ACTION_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION2_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0);
    when(actionService.createAction(any(Action.class))).thenReturn(action);

    ResultActions actions = mockMvc.perform(postJson("/actions", ACTION_VALIDJSON));

    actions.andExpect(status().isCreated())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("createAction"))
            .andExpect(jsonPath("$.id", is(ACTIONID_2.toString())))
            .andExpect(jsonPath("$.caseId", is(ACTION_CASEID.toString())))
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
    when(actionCaseService.findActionCase(ACTION_CASEID)).thenReturn(new ActionCase());


    ActionType actionType = new ActionType(1, ACTION2_ACTIONTYPENAME, ACTION2_ACTIONTYPEDESC,
            ACTION2_ACTIONTYPEHANDLER, ACTION2_ACTIONTYPECANCEL, ACTION2_RESPONSEREQUIRED);

    Action action = new Action(ACTION_PK, ACTIONID_2, ACTION_CASEID, ACTION_CASEFK, ACTION2_PLAN_FK, ACTION2_RULE_FK,
            ACTION_CREATEDBY, ACTION2_MANUALLY_CREATED, actionType, ACTION2_PRIORITY, ACTION2_SITUATION,
            ACTION3_ACTIONSTATE, ACTION_CREATEDDATE_TIMESTAMP, ACTION_UPDATEDDATE_TIMESTAMP, 0);
    List<Action> result = new ArrayList<>();
    result.add(action);
    when(actionService.cancelActions(ACTION_CASEID)).thenReturn(result);


    ResultActions actions = mockMvc.perform(putJson(String.format("/actions/case/%s/cancel", ACTION_CASEID),
            ""));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionEndpoint.class))
            .andExpect(handler().methodName("cancelActions"))
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(ACTIONID_2.toString())))
            .andExpect(jsonPath("$[0].caseId", is(ACTION_CASEID.toString())))
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
