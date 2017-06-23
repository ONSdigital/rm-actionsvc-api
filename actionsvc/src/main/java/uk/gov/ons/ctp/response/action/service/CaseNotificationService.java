package uk.gov.ons.ctp.response.action.service;

import java.util.List;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification;

/**
 * Service to persist case life cycle event notifications.
 *
 */
public interface CaseNotificationService {

  /**
   * Deal with case life cycle notification
   *
   * @param notifications List of CaseNotification message objects.
   * @throws CTPException if action state transition error
   */
  void acceptNotification(List<CaseNotification> notifications) throws CTPException;

}
