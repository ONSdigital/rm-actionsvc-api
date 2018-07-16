package uk.gov.ons.ctp.response.action.representation;

import static org.junit.Assert.assertEquals;
import static uk.gov.ons.ctp.response.action.representation.Situation.MAXIMUM_LENGTH;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ons.ctp.response.action.representation.exception.InvalidSituationException;

public class SituationTest {

  private static final String VALID = "Notify Sms Not Sent";
  private static final String TOO_LONG = StringUtils.repeat("a", MAXIMUM_LENGTH + 1);
  private static final String MAX_LENGTH = StringUtils.repeat("x", MAXIMUM_LENGTH);

  @Rule public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testToStringReturnsTheValue() {
    Situation situation = new Situation(VALID);

    assertEquals(situation.toString(), VALID);
  }

  @Test
  public void testItThrowsAnExceptionIfSituationIsTooLong() {
    expectedEx.expect(InvalidSituationException.class);
    expectedEx.expectMessage(InvalidSituationException.tooLong(TOO_LONG).getMessage());

    new Situation(TOO_LONG);
  }

  @Test
  public void testItDoesNotThrowAnExceptionIfTheSituationIsTheMaximumLength() {
    new Situation(MAX_LENGTH);
  }
}
