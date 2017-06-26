package uk.gov.ons.ctp.response.action.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.message.CaseNotificationReceiver;
import uk.gov.ons.ctp.response.action.service.CaseNotificationService;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotifications;

import java.util.stream.Collectors;

/**
 * Message end point for Case notification life cycle messages, please see flows.xml.
 */
@MessageEndpoint
@Slf4j
public class CaseNotificationReceiverImpl implements CaseNotificationReceiver {

  @Autowired
  private CaseNotificationService caseNotificationService;

  // TODO CTPA-1340
  @Override
  @ServiceActivator(inputChannel = "caseNotificationTransformed", adviceChain = "caseNotificationRetryAdvice")
  public void acceptNotification(CaseNotifications caseNotifications) throws CTPException {
    log.debug("Receiving case notifications for case ids {}", caseNotifications.getCaseNotifications().stream()
              .map(cn -> cn.getCaseId().toString())
              .collect(Collectors.joining(",")));
    caseNotificationService.acceptNotification(caseNotifications.getCaseNotifications());
  }
}
