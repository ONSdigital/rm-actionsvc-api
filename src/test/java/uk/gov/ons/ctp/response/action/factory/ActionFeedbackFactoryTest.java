package uk.gov.ons.ctp.response.action.factory;

import org.junit.Test;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.message.feedback.Outcome;
import uk.gov.ons.ctp.response.action.representation.Situation;

import static org.junit.Assert.assertEquals;

public class ActionFeedbackFactoryTest {

  public static final String ACTION_ID = "08073f47-1d8d-44cf-b035-31c398ec46bf";
  public static final String SITUATION_STRING = "NotifyGateway";
  public static final Outcome OUTCOME = Outcome.CANCELLATION_ACCEPTED;

  @Test
  public void testItCreatesAnActionFeedbackInstance() {
    final Situation situation = new Situation(SITUATION_STRING);

    ActionFeedback actionFeedback = ActionFeedbackFactory.create(ACTION_ID, situation, OUTCOME);

    assertEquals(ACTION_ID, actionFeedback.getActionId());
    assertEquals(SITUATION_STRING, actionFeedback.getSituation());
    assertEquals(Outcome.CANCELLATION_ACCEPTED, actionFeedback.getOutcome());
  }
}
