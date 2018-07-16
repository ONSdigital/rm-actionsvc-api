package uk.gov.ons.ctp.response.action.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.message.feedback.Outcome;
import uk.gov.ons.ctp.response.action.representation.Situation;

public class ActionFeedbackFactoryTest {

  private static final String ACTION_ID = "08073f47-1d8d-44cf-b035-31c398ec46bf";
  private static final String SITUATION_STRING = "NotifyGateway";
  private static final Outcome OUTCOME = Outcome.CANCELLATION_ACCEPTED;

  @Test
  public void testItCreatesAnActionFeedbackInstance() {
    final Situation situation = new Situation(SITUATION_STRING);

    ActionFeedback actionFeedback = ActionFeedbackFactory.create(ACTION_ID, situation, OUTCOME);

    assertEquals(ACTION_ID, actionFeedback.getActionId());
    assertEquals(SITUATION_STRING, actionFeedback.getSituation());
    assertEquals(Outcome.CANCELLATION_ACCEPTED, actionFeedback.getOutcome());
  }
}
