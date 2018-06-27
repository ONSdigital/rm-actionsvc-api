package uk.gov.ons.ctp.response.action.representation;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object for representation. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionPlanJobRequestDTO {

  private static final int CREATED_BY_MAX = 50;
  private static final int CREATED_BY_MIN = 2;

  @NotNull
  @Size(min = CREATED_BY_MIN, max = CREATED_BY_MAX)
  @ApiModelProperty(required = true)
  private String createdBy;
}
