package uk.gov.ons.ctp.response.action.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.domain.model.OutcomeCategory;
import uk.gov.ons.ctp.response.action.domain.model.OutcomeHandlerId;
import uk.gov.ons.ctp.response.action.domain.repository.ActionRepository;
import uk.gov.ons.ctp.response.action.domain.repository.OutcomeCategoryRepository;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.representation.ActionDTO.ActionState;
import uk.gov.ons.ctp.response.action.service.CaseSvcClientService;
import uk.gov.ons.ctp.response.action.service.FeedbackService;
import uk.gov.ons.ctp.response.casesvc.representation.CategoryDTO;

import java.util.UUID;

/**
 * Accept feedback from handlers
 */
@Slf4j
@Service
public class FeedbackServiceImpl implements FeedbackService {

  private static final int TRANSACTION_TIMEOUT = 30;

  @Autowired
  private CaseSvcClientService caseSvcClientService;

  @Autowired
  private ActionRepository actionRepo;

  @Autowired
  private OutcomeCategoryRepository outcomeCategoryRepository;

  @Autowired
  private StateTransitionManager<ActionState, ActionDTO.ActionEvent> actionSvcStateTransitionManager;

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false, timeout = TRANSACTION_TIMEOUT)
  @Override
  public void acceptFeedback(ActionFeedback feedback) throws CTPException {
    UUID actionId = UUID.fromString(feedback.getActionId());

    Action action = actionRepo.findById(actionId);

    if (action != null) {
      ActionDTO.ActionEvent outcomeEvent = ActionDTO.ActionEvent.valueOf(feedback.getOutcome().name());

      if (outcomeEvent != null) {
        String situation = feedback.getSituation();

        try {
          ActionDTO.ActionState nextState = actionSvcStateTransitionManager.transition(action.getState(),
              outcomeEvent);
          updateAction(action, nextState, situation);
        } catch (RuntimeException re) {
          log.error(
              "Feedback Service unable to effect state transition. Ignoring feedback. Reason: {}" + re.getMessage());
          throw re;
        }

        String handler = action.getActionType().getHandler();
        OutcomeHandlerId outcomeHandlerId = OutcomeHandlerId.builder().handler(handler).actionOutcome(outcomeEvent)
            .build();
        OutcomeCategory outcomeCategory = outcomeCategoryRepository.findOne(outcomeHandlerId);
        if (outcomeCategory != null) {
          CategoryDTO.CategoryName category = CategoryDTO.CategoryName.valueOf(outcomeCategory.getEventCategory());
          caseSvcClientService.createNewCaseEvent(action, category);
        }
      } else {
        log.error("Feedback Service unable to decipher the outcome {} from feedback - ignoring this feedback",
            feedback.getOutcome());
        throw new RuntimeException("Outcome " + feedback.getOutcome() + " unknown");
      }
    } else {
      log.error("Feedback Service unable to find action id {} from feedback - ignoring this feedback",
          feedback.getActionId());
      throw new RuntimeException("ActionID " + feedback.getActionId() + " unknown");
    }
  }

  /**
   * Update the action
   *
   * @param action the action to update
   * @param nextState the state to transition to
   * @param situation the situation provided by the feedback
   */
  private void updateAction(Action action, ActionDTO.ActionState nextState, String situation) {
    action.setSituation(situation);
    action.setState(nextState);
    action.setUpdatedDateTime(DateTimeUtil.nowUTC());
    actionRepo.saveAndFlush(action);
  }
}
