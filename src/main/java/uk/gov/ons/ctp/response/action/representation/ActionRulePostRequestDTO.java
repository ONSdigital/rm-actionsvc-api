package uk.gov.ons.ctp.response.action.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * Domain model object for representation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionRulePostRequestDTO {

  @NotNull
  private UUID actionPlanId;

  @NotNull
  @Size(max = 100)
  private String actionTypeName;

  @NotNull
  @Size(max = 100)
  private String name;

  @NotNull
  @Size(max = 250)
  private String description;

  @NotNull
  private Integer daysOffset;

  @Min(value = 1)
  @Max(value = 5)
  private Integer priority;
}