package uk.gov.ons.ctp.response.action.representation;

import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object for representation. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionPostRequestDTO {

  @NotNull
  @ApiModelProperty(required = true)
  private UUID caseId;

  @NotNull
  @ApiModelProperty(required = true)
  private String actionTypeName;

  @NotNull
  @ApiModelProperty(required = true)
  private String createdBy;

  private Integer priority;
}
