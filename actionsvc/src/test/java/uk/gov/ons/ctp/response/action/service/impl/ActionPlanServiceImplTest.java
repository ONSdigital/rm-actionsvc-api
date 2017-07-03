package uk.gov.ons.ctp.response.action.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanRepository;

import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for the ActionPlanJobServiceImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionPlanServiceImplTest {

  @Mock
  private AppConfig appConfig;

  @Mock
  private ActionPlanRepository actionPlanRepo;

  @InjectMocks
  private ActionPlanServiceImpl actionPlanServiceImpl;

  private static final UUID ACTION_PLAN_1_ID = UUID.fromString("e71002ac-3575-47eb-b87f-cd9db92bf9a7");

  /**
   * Before the test
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * @throws Exception oops
   */
  @Test
  public void testUpdateActionPlanNoChange() throws Exception {
    // set up dummy data
    List<ActionPlan> persistedActionPlans = FixtureHelper.loadClassFixtures(ActionPlan[].class);
    ActionPlan blankActionPlan = ActionPlan.builder().build();

    // wire up mock responses
    Mockito.when(actionPlanRepo.findOne(1)).thenReturn(persistedActionPlans.get(0));

    // let it roll
    actionPlanServiceImpl.updateActionPlan(ACTION_PLAN_1_ID, blankActionPlan);

    // assert the right calls were made
    verify(actionPlanRepo).findById(ACTION_PLAN_1_ID);
    verify(actionPlanRepo, times(0)).save(any(ActionPlan.class));
  }

  /**
   * @throws Exception oops
   */
  @Test
  public void testUpdateActionPlanChangeDesc() throws Exception {
    // set up dummy data
    List<ActionPlan> persistedActionPlans = FixtureHelper.loadClassFixtures(ActionPlan[].class);
    ActionPlan actionPlanWithDesc = ActionPlan.builder().description("this is a test").build();

    // wire up mock responses
    Mockito.when(actionPlanRepo.findById(ACTION_PLAN_1_ID)).thenReturn(persistedActionPlans.get(0));


    // let it roll
    actionPlanServiceImpl.updateActionPlan(ACTION_PLAN_1_ID, actionPlanWithDesc);

    // assert the right calls were made
    verify(actionPlanRepo).findById(ACTION_PLAN_1_ID);
    verify(actionPlanRepo, times(1)).save(any(ActionPlan.class));
  }
  
  /**
   * @throws Exception oops
   */
  @Test
  public void testUpdateActionPlanChangeLastGoodRunDateTime() throws Exception {
    // set up dummy data
    List<ActionPlan> persistedActionPlans = FixtureHelper.loadClassFixtures(ActionPlan[].class);
    ActionPlan actionPlanWithLastGoodRunDateTime = ActionPlan.builder().lastRunDateTime(DateTimeUtil.nowUTC()).build();

    // wire up mock responses
    Mockito.when(actionPlanRepo.findById(ACTION_PLAN_1_ID)).thenReturn(persistedActionPlans.get(0));

    // let it roll
    actionPlanServiceImpl.updateActionPlan(ACTION_PLAN_1_ID, actionPlanWithLastGoodRunDateTime);

    // assert the right calls were made
    verify(actionPlanRepo).findById(ACTION_PLAN_1_ID);
    verify(actionPlanRepo, times(1)).save(any(ActionPlan.class));
  }
}
