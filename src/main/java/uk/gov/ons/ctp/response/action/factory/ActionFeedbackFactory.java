package uk.gov.ons.ctp.response.action.factory;

import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.message.feedback.Outcome;
import uk.gov.ons.ctp.response.action.representation.Situation;

public class ActionFeedbackFactory {
  public static ActionFeedback create(String actionId, Situation situation, Outcome outcome) {
    return new ActionFeedback(actionId, situation.toString(), outcome);
  }
}
