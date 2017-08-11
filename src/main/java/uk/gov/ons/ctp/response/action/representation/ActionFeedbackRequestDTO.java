package uk.gov.ons.ctp.response.action.representation;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.action.message.feedback.Outcome;

import javax.validation.constraints.NotNull;

/**
 * Domain model object for representation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionFeedbackRequestDTO {

    @NotNull
    @ApiModelProperty(required = true)
    private String situation;

    @NotNull
    @ApiModelProperty(required = true)
    private Outcome outcome;
}
