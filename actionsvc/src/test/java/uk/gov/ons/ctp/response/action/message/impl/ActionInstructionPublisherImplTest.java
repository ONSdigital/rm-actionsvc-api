package uk.gov.ons.ctp.response.action.message.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.response.action.message.instruction.ActionCancel;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;

@RunWith(MockitoJUnitRunner.class)
public class ActionInstructionPublisherImplTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private ActionInstructionPublisherImpl actionInstructionPublisherImpl;

  @Test
  public void sendActionInstructionsTest() throws Exception {
    List<ActionRequest> actionRequests = FixtureHelper.loadClassFixtures(ActionRequest[].class);
    List<ActionCancel> actionCancels = FixtureHelper.loadClassFixtures(ActionCancel[].class);

    actionInstructionPublisherImpl.sendActionInstructions("test", actionRequests, actionCancels);

    verify(rabbitTemplate, times(2)).convertAndSend(any(String.class), any(Object.class));
  }

  @Test
  public void sendActionInstructionsTestNoRequests() throws Exception {
    List<ActionCancel> actionCancels = FixtureHelper.loadClassFixtures(ActionCancel[].class);

    actionInstructionPublisherImpl.sendActionInstructions("test", null, actionCancels);

    verify(rabbitTemplate, times(1)).convertAndSend(any(String.class), any(Object.class));
  }

  @Test
  public void sendActionInstructionsTestNoCancels() throws Exception {
    List<ActionRequest> actionRequests = FixtureHelper.loadClassFixtures(ActionRequest[].class);

    actionInstructionPublisherImpl.sendActionInstructions("test", actionRequests, null);

    verify(rabbitTemplate, times(1)).convertAndSend(any(String.class), any(Object.class));
  }
  
  @Test
  public void sendActionInstructionsTestEmptyLists() throws Exception {
    List<ActionRequest> actionRequests = new ArrayList<>();
    List<ActionCancel> actionCancels = new ArrayList<>();

    actionInstructionPublisherImpl.sendActionInstructions("test", actionRequests, actionCancels);

    verify(rabbitTemplate, times(0)).convertAndSend(any(String.class), any(Object.class));
  }

}
