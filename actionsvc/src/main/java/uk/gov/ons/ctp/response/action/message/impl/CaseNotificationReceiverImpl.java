package uk.gov.ons.ctp.response.action.message.impl;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.cobertura.CoverageIgnore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.message.CaseNotificationReceiver;
import uk.gov.ons.ctp.response.action.service.CaseNotificationService;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification;

/**
 * Message end point for Case notification life cycle messages, please see flows.xml.
 */
@CoverageIgnore
@MessageEndpoint
@Slf4j
public class CaseNotificationReceiverImpl implements CaseNotificationReceiver {

  @Autowired
  private CaseNotificationService caseNotificationService;

  @Override
  @ServiceActivator(inputChannel = "caseNotificationTransformed", adviceChain = "caseNotificationRetryAdvice")
  public void acceptNotification(CaseNotification caseNotification) throws CTPException {
    log.debug("Receiving case notification for case id {}", caseNotification.getCaseId());
    caseNotificationService.acceptNotification(caseNotification);
  }
}
