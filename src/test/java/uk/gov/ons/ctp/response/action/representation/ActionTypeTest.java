package uk.gov.ons.ctp.response.action.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

public class ActionTypeTest {

  @Test
  public void ensureSocialIcf() throws Exception {
    ActionType actionType = ActionType.SOCIALICF;
    assertThat(actionType.toString(), is(equalTo("SOCIALICF")));

    ActionType at = ActionType.valueOf("SOCIALICF");
    assertThat(at, is(ActionType.SOCIALICF));
  }
}
