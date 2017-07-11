package uk.gov.ons.ctp.response.action.service;

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
   * @param notification a CaseNotification message object.
   * @throws CTPException if action state transition error
   */
  void acceptNotification(CaseNotification notification) throws CTPException;

}
