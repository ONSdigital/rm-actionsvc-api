package uk.gov.ons.ctp.response.action.representation;

public enum ActionType {
  BSNOT("BSNOT"),
  BSREM("BSREM"),
  BSSNE("BSSNE"),
  BSRE("BSRE"),
  BSRL("BSRL"),
  BSNE("BSNE"),
  BSNL("BSNL"),
  SOCIALNOT("SOCIALNOT"),
  SOCIALREM("SOCIALREM"),
  SOCIALSNE("SOCIALSNE"),
  SOCIALPRENOT("SOCIALPRENOT"),
  SOCIALICF("SOCIALICF");

  private String name;

  ActionType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
