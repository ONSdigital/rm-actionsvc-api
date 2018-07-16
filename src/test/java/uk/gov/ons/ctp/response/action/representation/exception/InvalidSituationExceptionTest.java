package uk.gov.ons.ctp.response.action.representation.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InvalidSituationExceptionTest {
  @Test
  public void testTooLongMessageIsDescriptive() {
    final InvalidSituationException exception = InvalidSituationException.tooLong("long message");

    assertEquals(
        exception.getMessage(), "Situation can have a maximum length of 100; got \"long message\"");
  }
}
