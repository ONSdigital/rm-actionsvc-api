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
import uk.gov.ons.ctp.response.action.domain.model.ActionPlanJob;
import uk.gov.ons.ctp.response.action.representation.ActionPlanJobDTO;
import uk.gov.ons.ctp.response.action.service.ActionPlanJobService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.common.MvcHelper.getJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static uk.gov.ons.ctp.response.action.service.impl.ActionPlanJobServiceImpl.CREATED_BY_SYSTEM;
import static uk.gov.ons.ctp.response.action.service.impl.ActionPlanJobServiceImpl.NO_ACTIONPLAN_MSG;

public class ActionPlanJobEndpointUnitTest {

  private static final UUID ACTIONPLANID = UUID.fromString("e71002ac-3575-47eb-b87f-cd9db92bf9a7");
  private static final UUID ACTIONPLANID_NOTFOUND = UUID.fromString("e71002ac-3575-47eb-b87f-cd9db92bf9a8");
  private static final UUID ACTION_PLAN_JOB_ID_1 = UUID.fromString("5381731e-e386-41a1-8462-26373744db81");
  private static final UUID ACTION_PLAN_JOB_ID_2 = UUID.fromString("5381731e-e386-41a1-8462-26373744db82");
  private static final UUID ACTION_PLAN_JOB_ID_3 = UUID.fromString("5381731e-e386-41a1-8462-26373744db83");

  private static final String CREATED_DATE_TIME_ACTION_PLAN_JOB_ID_1 = "2017-05-15T11:00:00.000+0100";
  private static final String CREATED_DATE_TIME_ACTION_PLAN_JOB_ID_2 = "2017-05-16T11:00:00.000+0100";
  private static final String CREATED_DATE_TIME_ACTION_PLAN_JOB_ID_3 = "2017-05-17T11:00:00.000+0100";
  private static final String UPDATED_DATE_TIME_ACTION_PLAN_JOB_ID_1 = "2017-05-15T12:00:00.000+0100";
  private static final String UPDATED_DATE_TIME_ACTION_PLAN_JOB_ID_2 = "2017-05-16T12:00:00.000+0100";
  private static final String UPDATED_DATE_TIME_ACTION_PLAN_JOB_ID_3 = "2017-05-17T12:00:00.000+0100";

  @InjectMocks
  private ActionPlanJobEndpoint actionPlanJobEndpoint;

  @Mock
  private ActionPlanJobService actionPlanJobService;

  @Spy
  private MapperFacade mapperFacade = new ActionBeanMapper();

  private MockMvc mockMvc;

  private List<ActionPlanJob> actionPlanJobs;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders
            .standaloneSetup(actionPlanJobEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();

    actionPlanJobs =  FixtureHelper.loadClassFixtures(ActionPlanJob[].class);
  }

  /**
   * A Test to retrieve action plan jobs for an actionplan that does not exist
   * @throws Exception exception thrown when getJson does
   */
  @Test
  public void findActionPlanJobsForActionPlanNotFound() throws Exception {
    when(actionPlanJobService.findActionPlanJobsForActionPlan(ACTIONPLANID_NOTFOUND)).thenThrow(
            new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, NO_ACTIONPLAN_MSG, ACTIONPLANID_NOTFOUND));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s/jobs", ACTIONPLANID_NOTFOUND)));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionPlanJobEndpoint.class))
            .andExpect(handler().methodName("findAllActionPlanJobsByActionPlanId"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", is(is(String.format(NO_ACTIONPLAN_MSG, ACTIONPLANID_NOTFOUND)))))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  /**
   * A Test to retrieve action plan jobs for an actionplan that does exist BUT no action plan job
   * @throws Exception exception thrown when getJson does
   */
  @Test
  public void findActionPlanJobsForActionPlanNoActionPlanJob() throws Exception {
    when(actionPlanJobService.findActionPlanJobsForActionPlan(ACTIONPLANID)).thenReturn(new ArrayList<>());

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s/jobs", ACTIONPLANID)));

    actions.andExpect(status().isNoContent())
            .andExpect(handler().handlerType(ActionPlanJobEndpoint.class))
            .andExpect(handler().methodName("findAllActionPlanJobsByActionPlanId"));
  }

  /**
   * A Test to retrieve action plan jobs for an actionplan that does exist AND action plan jobs exist
   * @throws Exception exception thrown when getJson does
   */
  @Test
  public void findActionPlanJobFoundForActionPlan() throws Exception {
    when(actionPlanJobService.findActionPlanJobsForActionPlan(ACTIONPLANID)).thenReturn(actionPlanJobs);

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/%s/jobs", ACTIONPLANID)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionPlanJobEndpoint.class))
            .andExpect(handler().methodName("findAllActionPlanJobsByActionPlanId"))
            .andExpect(jsonPath("$", Matchers.hasSize(3)))
            .andExpect(jsonPath("$[0].*", hasSize(6)))
            .andExpect(jsonPath("$[1].*", hasSize(6)))
            .andExpect(jsonPath("$[2].*", hasSize(6)))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(ACTION_PLAN_JOB_ID_1.toString(),
                    ACTION_PLAN_JOB_ID_2.toString(), ACTION_PLAN_JOB_ID_3.toString())))
            .andExpect(jsonPath("$[*].actionPlanId", containsInAnyOrder(ACTIONPLANID.toString(),
                    ACTIONPLANID.toString(), ACTIONPLANID.toString())))
            .andExpect(jsonPath("$[*].createdBy", containsInAnyOrder(CREATED_BY_SYSTEM, CREATED_BY_SYSTEM,
                    CREATED_BY_SYSTEM)))
            .andExpect(jsonPath("$[*].state", containsInAnyOrder(ActionPlanJobDTO.ActionPlanJobState.COMPLETED.name(),
                    ActionPlanJobDTO.ActionPlanJobState.STARTED.name(),
                    ActionPlanJobDTO.ActionPlanJobState.SUBMITTED.name())))
            .andExpect(jsonPath("$[*].createdDateTime", containsInAnyOrder(CREATED_DATE_TIME_ACTION_PLAN_JOB_ID_1, CREATED_DATE_TIME_ACTION_PLAN_JOB_ID_2, CREATED_DATE_TIME_ACTION_PLAN_JOB_ID_3)))
            .andExpect(jsonPath("$[*].updatedDateTime", containsInAnyOrder(UPDATED_DATE_TIME_ACTION_PLAN_JOB_ID_1, UPDATED_DATE_TIME_ACTION_PLAN_JOB_ID_2, UPDATED_DATE_TIME_ACTION_PLAN_JOB_ID_3)));
  }

//  /**
//   * A Test
//   * @throws Exception exception thrown
//   */
//  @Test
//  public void findActionPlanJobNotFound() throws Exception {
//    when(actionPlanJobService.findActionPlanJob(NON_EXISTING_ACTIONPLANJOBID)).thenReturn(Optional.empty());
//
//    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/jobs/%s",
//            NON_EXISTING_ACTIONPLANJOBID)));
//
//    actions.andExpect(status().isNotFound())
//            .andExpect(handler().handlerType(ActionPlanJobEndpoint.class))
//            .andExpect(handler().methodName("findActionPlanJobById"))
//            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
//            .andExpect(jsonPath("$.error.message", isA(String.class)))
//            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
//  }

//  /**
//   * A Test
//   * @throws Exception exception thrown
//   */
//  @Test
//  public void findActionPlanUnCheckedException() throws Exception {
//    when(actionPlanJobService.findActionPlanJob(UNCHECKED_EXCEPTION_ACTIONPLANJOBID)).thenThrow(
//            new IllegalArgumentException(OUR_EXCEPTION_MESSAGE));
//
//    ResultActions actions = mockMvc.perform(getJson(String.format("/actionplans/jobs/%s",
//            UNCHECKED_EXCEPTION_ACTIONPLANJOBID)));
//
//    actions.andExpect(status().is5xxServerError())
//            .andExpect(handler().handlerType(ActionPlanJobEndpoint.class))
//            .andExpect(handler().methodName("findActionPlanJobById"))
//            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.SYSTEM_ERROR.name())))
//            .andExpect(jsonPath("$.error.message", is(OUR_EXCEPTION_MESSAGE)))
//            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
//  }

/*  *//**
   * A Test
   *//* TODO: Not yet implemented
  @Test
  public void executeActionPlanBadJsonProvided() throws Exception {
    ResultActions actions = mockMvc.perform(postJson(String.format("/actionplans/%s/jobs", ACTIONPLANID),
    ACTIONPLANJOB_INVALIDJSON));

    actions.andExpect(status().isBadRequest())
            .andExpect(handler().handlerType(ActionPlanJobEndpoint.class))
            .andExpect(handler().methodName("executeActionPlan"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
            .andExpect(jsonPath("$.error.message", is(INVALID_JSON)))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }*/

/*  *//**
   * A Test
   *//* TODO: Not yet implemented
  @Test
  public void executeActionPlanGoodJsonProvided() throws Exception {
    when(actionPlanJobService.createAndExecuteActionPlanJob(any(ActionPlanJob.class))).thenReturn(Optional.of(
            new ActionPlanJob(ACTIONPLANJOBID, ACTIONPLANJOBID_ACTIONPLANFK, ACTIONPLANJOBID_CREATED_BY,
            ACTIONPLANJOBID_STATE, ACTIONPLANJOBID_CREATEDDATE_TIMESTAMP, ACTIONPLANJOBID_UPDATED_DATE_TIMESTAMP)));

    ResultActions actions = mockMvc.perform(postJson(String.format("/actionplans/%s/jobs", ACTIONPLANID),
            ACTIONPLANJOB_VALIDJSON));

    actions.andExpect(status().isCreated())
            .andExpect(handler().handlerType(ActionPlanJobEndpoint.class))
            .andExpect(handler().methodName("executeActionPlan"))
            .andExpect(jsonPath("$.actionPlanJobPK", is(ACTIONPLANJOBID)))
            .andExpect(jsonPath("$.id", is(ACTIONPLANJOBID_ACTIONPLANFK)))
            .andExpect(jsonPath("$.createdBy", is(ACTIONPLANJOBID_CREATED_BY)))
            .andExpect(jsonPath("$.state", is(ACTIONPLANJOBID_STATE.name())))
            .andExpect(jsonPath("$.createdDateTime", is(CREATED_DATE_TIME)))
            .andExpect(jsonPath("$.updatedDateTime", is(UPDATED_DATE_TIME)));
  }*/

}
