package uk.gov.ons.ctp.response.action.representation;

import uk.gov.ons.ctp.response.action.representation.exception.InvalidSituationException;

public class Situation {
  public static final int MAXIMUM_LENGTH = 100;
  private final String situation;

  public Situation(String situation) throws InvalidSituationException {
    if (situation.length() > MAXIMUM_LENGTH) {
      throw InvalidSituationException.tooLong(situation);
    }

    this.situation = situation;
  }

  @Override
  public String toString() {
    return situation;
  }
}
