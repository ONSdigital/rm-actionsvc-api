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
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.model.ActionRule;
import uk.gov.ons.ctp.response.action.domain.model.ActionType;
import uk.gov.ons.ctp.response.action.service.ActionPlanService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.common.MvcHelper.getJson;
import static uk.gov.ons.ctp.common.MvcHelper.putJson;
import static uk.gov.ons.ctp.common.error.RestExceptionHandler.PROVIDED_JSON_INCORRECT;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

/**
 * Unit tests for ActionPlan endpoint
 */
public class ActionPlanEndpointUnitTest {

  private static final boolean ACTIONTYPE_CANCANCEL = true;
  private static final boolean ACTIONTYPE_RESPONSEREQUIRED = true;

  private static final Integer ACTIONPLANPK = 1;

  private static final Integer ACTIONRULE_PRIORITY = 1;
  private static final Integer ACTIONRULE_SURVEYDATEDAYSOFFSET = 1;
  private static final Integer ACTIONPLANID_WITHNOACTIONRULE = 13;
  private static final Integer NON_EXISTING_ACTIONPLANID = 998;
  private static final Integer UNCHECKED_EXCEPTION = 999;

  private static final UUID ACTIONPLAN1_ID = UUID.fromString("e71002ac-3575-47eb-b87f-cd9db92bf9a7");
  private static final UUID ACTIONPLAN2_ID = UUID.fromString("0009e978-0932-463b-a2a1-b45cb3ffcb2a");
  private static final UUID ACTIONPLAN3_ID = UUID.fromString("0009e978-0932-463b-a2a1-b45cb3ffcb2b");
  private static final String ACTIONPLAN1_NAME = "HH_APL1";
  private static final String ACTIONPLAN2_NAME = "HGH_APL1";
  private static final String ACTIONPLAN3_NAME = "CH_APL1";
  private static final String ACTIONPLAN1_DESC = "Household Action Plan 1";
  private static final String ACTIONPLAN2_DESC = "Hotel and Guest House Action Plan 1";
  private static final String ACTIONPLAN3_DESC = "Care Home Action Plan 1";
  private static final String ACTIONRULE_ACTIONTYPENAME = "Action Type Name";
  private static final String ACTIONRULE_DESCRIPTION = "This is a Test Action Rule";
  private static final String ACTIONRULE_NAME = "Test Action Rule";
  private static final String ACTIONTYPE_NAME = "Action Type Name";
  private static final String ACTIONTYPE_DESC = "Action Type Desc";
  private static final String ACTIONTYPE_HANDLER = "Field";
  private static final String CREATED_BY = "whilep1";
  private static final String LAST_RUN_DATE_TIME = "2016-03-09T11:15:48.023+0000";
  private static final String OUR_EXCEPTION_MESSAGE = "this is what we throw";

  private static final String ACTIONPLAN_JSON = "{\"id\":\"e71002ac-3575-47eb-b87f-cd9db92bf9a7\",\"name\":\"HH\", \"description\":\"philippetesting\","
          +"\"createdBy\":\"SYSTEM\", \"lastRunDateTime\":null}";
  private static final String ACTIONPLAN_INVALIDJSON = "{\"some\":\"joke\"}";

  private static final Timestamp ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP = Timestamp
          .valueOf("2016-03-09 11:15:48.023286");

  @InjectMocks
  private ActionPlanEndpoint actionPlanEndpoint;

  @Mock
  private ActionPlanService actionPlanService;

  private MockMvc mockMvc;

  @Spy
  private MapperFacade mapperFacade = new ActionBeanMapper();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders
            .standaloneSetup(actionPlanEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();
  }

  /**
   * A Test
   */
  @Test
  public void findActionPlansFound() throws Exception {
    List<ActionPlan> result = new ArrayList<>();
    result.add(new ActionPlan(1, ACTIONPLAN1_ID, ACTIONPLAN1_NAME, ACTIONPLAN1_DESC, CREATED_BY,
            ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP));
    result.add(new ActionPlan(2, ACTIONPLAN2_ID, ACTIONPLAN2_NAME, ACTIONPLAN2_DESC, CREATED_BY,
            ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP));
    result.add(new ActionPlan(3, ACTIONPLAN3_ID, ACTIONPLAN3_NAME, ACTIONPLAN3_DESC, CREATED_BY,
            ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP));
    when(actionPlanService.findActionPlans()).thenReturn(result);

    ResultActions actions = mockMvc.perform(getJson("/actionplans"));

    System.out.println(actions.andReturn().getResponse().getContentAsString());

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("findActionPlans"))
            .andExpect(jsonPath("$", Matchers.hasSize(3)))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(ACTIONPLAN1_ID.toString(), ACTIONPLAN2_ID.toString(), ACTIONPLAN3_ID.toString())))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder(ACTIONPLAN1_NAME, ACTIONPLAN2_NAME, ACTIONPLAN3_NAME)))
            .andExpect(jsonPath("$[*].description", containsInAnyOrder(ACTIONPLAN1_DESC, ACTIONPLAN2_DESC, ACTIONPLAN3_DESC)))
            .andExpect(jsonPath("$[*].createdBy", containsInAnyOrder(CREATED_BY, CREATED_BY, CREATED_BY)))
            .andExpect(jsonPath("$[*].lastRunDateTime", containsInAnyOrder(LAST_RUN_DATE_TIME, LAST_RUN_DATE_TIME, LAST_RUN_DATE_TIME)));
  }

  /**
   * A Test
   */
  @Test
  public void findActionPlanFound() throws Exception {
    when(actionPlanService.findActionPlan(ACTIONPLANPK)).thenReturn(new ActionPlan(ACTIONPLANPK, ACTIONPLAN3_ID, ACTIONPLAN3_NAME, ACTIONPLAN3_DESC, CREATED_BY,
            ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s", ACTIONPLANPK)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("findActionPlanByActionPlanId"))
            .andExpect(jsonPath("$.id", is(ACTIONPLAN3_ID.toString())))
            .andExpect(jsonPath("$.name", is(ACTIONPLAN3_NAME)))
            .andExpect(jsonPath("$.description", is(ACTIONPLAN3_DESC)))
            .andExpect(jsonPath("$.createdBy", is(CREATED_BY)))
            .andExpect(jsonPath("$.lastRunDateTime", is(LAST_RUN_DATE_TIME)));
  }

  /**
   * A Test
   */
  @Test
  public void findActionPlanNotFound() throws Exception {
    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s", NON_EXISTING_ACTIONPLANID)));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("findActionPlanByActionPlanId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", isA(String.class)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * A Test
   */
  @Test
  public void findActionPlanUnCheckedException() throws Exception {
    when(actionPlanService.findActionPlan(UNCHECKED_EXCEPTION))
            .thenThrow(new IllegalArgumentException(OUR_EXCEPTION_MESSAGE));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s", UNCHECKED_EXCEPTION)));

    actions.andExpect(status().is5xxServerError())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("findActionPlanByActionPlanId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.SYSTEM_ERROR.name())))
            .andExpect(jsonPath("$.error.message", is(OUR_EXCEPTION_MESSAGE)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * A Test
   */
  @Test
  public void findActionRulesForActionPlanFound() throws Exception {
    when(actionPlanService.findActionPlan(ACTIONPLANPK)).thenReturn(new ActionPlan(ACTIONPLANPK, ACTIONPLAN3_ID, ACTIONPLAN3_NAME, ACTIONPLAN3_DESC, CREATED_BY,
            ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP));

    ActionType actionType = new ActionType(1, ACTIONTYPE_NAME, ACTIONTYPE_DESC, ACTIONTYPE_HANDLER,
            ACTIONTYPE_CANCANCEL, ACTIONTYPE_RESPONSEREQUIRED);
    List<ActionRule> result = new ArrayList<>();
    result.add(new ActionRule(1, ACTIONPLANPK, ACTIONRULE_PRIORITY, ACTIONRULE_SURVEYDATEDAYSOFFSET,
            actionType, ACTIONRULE_NAME, ACTIONRULE_DESCRIPTION));
    result.add(new ActionRule(2, ACTIONPLANPK, ACTIONRULE_PRIORITY, ACTIONRULE_SURVEYDATEDAYSOFFSET,
            actionType, ACTIONRULE_NAME, ACTIONRULE_DESCRIPTION));
    result.add(new ActionRule(3, ACTIONPLANPK, ACTIONRULE_PRIORITY, ACTIONRULE_SURVEYDATEDAYSOFFSET,
            actionType, ACTIONRULE_NAME, ACTIONRULE_DESCRIPTION));
    when(actionPlanService.findActionRulesForActionPlan(ACTIONPLANPK)).thenReturn(result);

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s/rules", ACTIONPLANPK)));

    System.out.println(actions.andReturn().getResponse().getContentAsString());

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("returnActionRulesForActionPlanId"))
            .andExpect(jsonPath("$", Matchers.hasSize(3)))
            .andExpect(jsonPath("$[*].actionTypeName", containsInAnyOrder(ACTIONRULE_ACTIONTYPENAME, ACTIONRULE_ACTIONTYPENAME, ACTIONRULE_ACTIONTYPENAME)))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder(ACTIONRULE_NAME, ACTIONRULE_NAME, ACTIONRULE_NAME)))
            .andExpect(jsonPath("$[*].description", containsInAnyOrder(ACTIONRULE_DESCRIPTION, ACTIONRULE_DESCRIPTION, ACTIONRULE_DESCRIPTION)))
            .andExpect(jsonPath("$[*].daysOffset", containsInAnyOrder(ACTIONRULE_SURVEYDATEDAYSOFFSET,
                    ACTIONRULE_SURVEYDATEDAYSOFFSET, ACTIONRULE_SURVEYDATEDAYSOFFSET)))
            .andExpect(jsonPath("$[*].priority", containsInAnyOrder(ACTIONRULE_PRIORITY, ACTIONRULE_PRIORITY, ACTIONRULE_PRIORITY)));
  }

  /**
   * A Test
   */
  @Test
  public void findNoActionRulesForActionPlan() throws Exception {
    when(actionPlanService.findActionPlan(ACTIONPLANID_WITHNOACTIONRULE)).thenReturn(new ActionPlan(ACTIONPLANPK, ACTIONPLAN3_ID, ACTIONPLAN3_NAME, ACTIONPLAN3_DESC, CREATED_BY,
            ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s/rules", ACTIONPLANID_WITHNOACTIONRULE)));

    actions.andExpect(status().isNoContent())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("returnActionRulesForActionPlanId"));
  }

  /**
   * A Test
   */
  @Test
  public void findActionRulesForNonExistingActionPlan() throws Exception {
    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s/rules", NON_EXISTING_ACTIONPLANID)));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("returnActionRulesForActionPlanId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", isA(String.class)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * A Test
   */
  @Test
  public void updateActionPlanNegativeScenarioInvalidJsonProvided() throws Exception {
    ResultActions actions = mockMvc.perform(putJson(String.format("/actionplans/%s", ACTIONPLANPK), ACTIONPLAN_INVALIDJSON));

    actions.andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("updateActionPlanByActionPlanId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
            .andExpect(jsonPath("$.error.message", is(PROVIDED_JSON_INCORRECT)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * A Test
   */
  @Test
  public void updateActionPlanHappyScenario() throws Exception {
    when(actionPlanService.updateActionPlan(any(Integer.class), any(ActionPlan.class))).thenReturn(new ActionPlan(ACTIONPLANPK, ACTIONPLAN3_ID, ACTIONPLAN3_NAME, ACTIONPLAN3_DESC, CREATED_BY,
            ACTIONPLAN_LAST_GOOD_RUN_DATE_TIMESTAMP));

    ResultActions actions = mockMvc.perform(putJson(String.format("/actionplans/%s", ACTIONPLANPK), ACTIONPLAN_JSON));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionPlanEndpoint.class))
            .andExpect(handler().methodName("updateActionPlanByActionPlanId"))
            .andExpect(jsonPath("$.id", is(ACTIONPLAN3_ID.toString())))
            .andExpect(jsonPath("$.name", is(ACTIONPLAN3_NAME)))
            .andExpect(jsonPath("$.description", is(ACTIONPLAN3_DESC)))
            .andExpect(jsonPath("$.createdBy", is(CREATED_BY)))
            .andExpect(jsonPath("$.lastRunDateTime", is(LAST_RUN_DATE_TIME)));
  }
}
