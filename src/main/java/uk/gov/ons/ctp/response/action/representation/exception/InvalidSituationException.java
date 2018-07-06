package uk.gov.ons.ctp.response.action.representation.exception;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.Situation;

public class InvalidSituationException extends CTPException {

  private static final String TOO_LONG_MESSAGE =
      "Situation can have a maximum length of %d; got \"%s\"";

  private InvalidSituationException(String message) {
    super(Fault.VALIDATION_FAILED, message);
  }

  public static InvalidSituationException tooLong(String situation) {
    return new InvalidSituationException(
        String.format(TOO_LONG_MESSAGE, Situation.MAXIMUM_LENGTH, situation));
  }
}
