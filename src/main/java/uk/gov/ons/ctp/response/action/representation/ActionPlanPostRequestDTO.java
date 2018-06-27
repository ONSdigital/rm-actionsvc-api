package uk.gov.ons.ctp.response.action.representation;

import java.util.HashMap;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object for representation */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionPlanPostRequestDTO {

  @NotNull
  @Size(max = 100)
  private String name;

  @NotNull
  @Size(max = 250)
  private String description;

  @NotNull
  @Size(max = 20)
  private String createdBy;

  private HashMap<String, String> selectors;
}
