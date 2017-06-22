package uk.gov.ons.ctp.response.action.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * Domain model object for representation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionPlanDTO {

  @NotNull
  private UUID id;

  private String name;

  private String description;

  private String createdBy;

  private Date lastGoodRunDateTime;
}
