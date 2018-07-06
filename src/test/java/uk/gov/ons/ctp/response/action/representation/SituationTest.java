package uk.gov.ons.ctp.response.action.representation;

import static org.junit.Assert.assertEquals;
import static uk.gov.ons.ctp.response.action.representation.Situation.MAXIMUM_LENGTH;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ons.ctp.response.action.representation.exception.InvalidSituationException;

public class SituationTest {

  public static final String VALID = "Notify Sms Not Sent";
  public static final String TOO_LONG = StringUtils.repeat("a", MAXIMUM_LENGTH + 1);
  public static final String MAX_LENGTH = StringUtils.repeat("x", MAXIMUM_LENGTH);

  @Rule public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testToStringReturnsTheValue() throws InvalidSituationException {
    Situation situation = new Situation(VALID);

    assertEquals(situation.toString(), VALID);
  }

  @Test
  public void testItThrowsAnExceptionIfSituationIsTooLong() throws InvalidSituationException {
    expectedEx.expect(InvalidSituationException.class);
    expectedEx.expectMessage(InvalidSituationException.tooLong(TOO_LONG).getMessage());

    new Situation(TOO_LONG);
  }

  @Test
  public void testItDoesNotThrowAnExceptionIfTheSituationIsTheMaximumLength()
      throws InvalidSituationException {
    new Situation(MAX_LENGTH);
  }
}
