package uk.gov.ons.ctp.response.action.representation.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import uk.gov.ons.ctp.common.error.CTPException;

public class InvalidSituationExceptionTest {
  @Test
  public void testInvalidSituationExceptionIsACTPException() {
    assertTrue(InvalidSituationException.tooLong("long message") instanceof CTPException);
  }

  @Test
  public void testTooLongMessageIsDescriptive() {
    final InvalidSituationException exception = InvalidSituationException.tooLong("long message");

    assertEquals(
        exception.getMessage(), "Situation can have a maximum length of 100; got \"long message\"");
  }

  @Test
  public void testInvalidExceptionIsAValidFailure() {
    CTPException exception = InvalidSituationException.tooLong("long message");
    assertEquals(exception.getFault(), CTPException.Fault.VALIDATION_FAILED);
  }
}
