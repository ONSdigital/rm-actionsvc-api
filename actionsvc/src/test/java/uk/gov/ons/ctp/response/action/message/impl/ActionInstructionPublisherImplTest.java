package uk.gov.ons.ctp.response.action.message.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.response.action.message.instruction.Action;
import uk.gov.ons.ctp.response.action.message.instruction.ActionCancel;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ctp.response.action.message.impl.ActionInstructionPublisherImpl.ACTION;
import static uk.gov.ons.ctp.response.action.message.impl.ActionInstructionPublisherImpl.BINDING;

/**
 * Tests for ActionInstructionPublisherImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionInstructionPublisherImplTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private ActionInstructionPublisherImpl actionInstructionPublisherImpl;

  /**
   * Build an ActionInstruction with One ActionRequest and send to queue.
   *
   * @throws Exception when loadClassFixtures does
   */
  @Test
  public void sendActionInstructionWithOneActionRequest() throws Exception {
    String handler = "test";
    List<ActionRequest> actionRequests = FixtureHelper.loadClassFixtures(ActionRequest[].class);
    ActionRequest requestToSend = actionRequests.get(0);

    actionInstructionPublisherImpl.sendActionInstruction(handler, requestToSend);

    ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<ActionInstruction> actionInstructionCaptor = ArgumentCaptor.forClass(ActionInstruction.class);

    verify(rabbitTemplate, times(1)).convertAndSend(routingKeyCaptor.capture(),
        actionInstructionCaptor.capture());
    assertEquals(String.format("%s%s%s", ACTION, handler, BINDING), routingKeyCaptor.getValue());
    ActionInstruction instructionSent = actionInstructionCaptor.getValue();
    assertNull(instructionSent.getActionCancel());
    assertNull(instructionSent.getActionUpdate());
    assertEquals(requestToSend, instructionSent.getActionRequest());
  }

  /**
   * Build an ActionInstruction with One ActionCancel and send to queue
   *
   * @throws Exception when loadClassFixtures does
   */
  @Test
  public void sendActionInstructionWithOneActionCancel() throws Exception {
    String handler = "test";
    List<ActionCancel> actionCancels = FixtureHelper.loadClassFixtures(ActionCancel[].class);
    ActionCancel cancelToSend = actionCancels.get(0);

    actionInstructionPublisherImpl.sendActionInstruction(handler, cancelToSend);

    ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<ActionInstruction> actionInstructionCaptor = ArgumentCaptor.forClass(ActionInstruction.class);

    verify(rabbitTemplate, times(1)).convertAndSend(routingKeyCaptor.capture(),
        actionInstructionCaptor.capture());
    assertEquals(String.format("%s%s%s", ACTION, handler, BINDING), routingKeyCaptor.getValue());
    ActionInstruction instructionSent = actionInstructionCaptor.getValue();
    assertNull(instructionSent.getActionRequest());
    assertNull(instructionSent.getActionUpdate());
    assertEquals(cancelToSend, instructionSent.getActionCancel());
  }

  /**
   * Build an ActionInstruction with Neither an ActionRequest Nor an ActionCancel and send to queue
   *
   * @throws Exception when loadClassFixtures does
   */
  @Test
  public void sendActionInstructionWithNoActionRequestNorCancel() throws Exception {
    String handler = "test";

    actionInstructionPublisherImpl.sendActionInstruction(handler, new Action());

    ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<ActionInstruction> actionInstructionCaptor = ArgumentCaptor.forClass(ActionInstruction.class);

    verify(rabbitTemplate, times(1)).convertAndSend(routingKeyCaptor.capture(),
        actionInstructionCaptor.capture());
    assertEquals(String.format("%s%s%s", ACTION, handler, BINDING), routingKeyCaptor.getValue());
    ActionInstruction instructionSent = actionInstructionCaptor.getValue();
    assertNull(instructionSent.getActionRequest());
    assertNull(instructionSent.getActionUpdate());
    assertNull(instructionSent.getActionCancel());
  }
}
