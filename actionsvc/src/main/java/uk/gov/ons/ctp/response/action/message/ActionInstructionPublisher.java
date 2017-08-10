package uk.gov.ons.ctp.response.action.message;

import uk.gov.ons.ctp.response.action.message.instruction.Action;

/**
 * Interface to publish ActionInstructions to downstream handlers
 */
public interface ActionInstructionPublisher {

  /**
   * The implementation is responsible for publishing ActionInstructions
   *
   * @param handler the handler that the outbound flow should send to - taken directly from the Actions ActionType
   * @param action the action to publish
   */
  void sendActionInstruction(String handler, Action action);
}
