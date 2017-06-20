package uk.gov.ons.ctp.response.action.representation;

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
public class ActionFeedbackDTO {

    private String actionId;

    @NotNull
    private String situation;

    @NotNull
    private Outcome outcome;
}
