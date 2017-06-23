package uk.gov.ons.ctp.response.action.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.domain.model.ActionCase;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;
import uk.gov.ons.ctp.response.action.domain.repository.ActionCaseRepository;
import uk.gov.ons.ctp.response.action.domain.repository.ActionPlanRepository;
import uk.gov.ons.ctp.response.action.service.ActionService;
import uk.gov.ons.ctp.response.action.service.CaseNotificationService;
import uk.gov.ons.ctp.response.action.service.CaseSvcClientService;
import uk.gov.ons.ctp.response.action.service.CollectionExerciseClientService;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDetailsDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Save to Action.Case table for case creation life cycle events, delete for
 * case close life cycle events.
 *
 */
@Service
@Slf4j
public class CaseNotificationServiceImpl implements CaseNotificationService {

  private static final int TRANSACTION_TIMEOUT = 30;

  @Autowired
  private ActionCaseRepository actionCaseRepo;

  @Autowired
  private ActionPlanRepository actionPlanRepo;

  @Autowired
  private ActionService actionService;

  @Autowired
  private CaseSvcClientService caseSvcClientServiceImpl;

  @Autowired
  private CollectionExerciseClientService collectionSvcClientServiceImpl;

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false, timeout = TRANSACTION_TIMEOUT)
  public void acceptNotification(List<CaseNotification> notifications) throws CTPException {
    notifications.forEach((notif) -> {
      UUID actionPlanId = UUID.fromString(notif.getActionPlanId());
      UUID caseId = UUID.fromString(notif.getCaseId());
      ActionPlan actionPlan = actionPlanRepo.findById(actionPlanId);

      if (actionPlan != null) {
        ActionCase actionCase = ActionCase.builder().actionPlanId(actionPlanId).actionPlanFK(actionPlan.getActionPlanPK()).id(caseId).build();
        switch (notif.getNotificationType()) {
          case REPLACED:
          case ACTIVATED:
            CollectionExerciseDTO collectionExercise = getCollectionExercise(notif);
            actionCase.setActionPlanStartDate(collectionExercise.getScheduledStartDateTime());
            actionCase.setActionPlanEndDate(collectionExercise.getScheduledEndDateTime());
            checkAndSaveCase(actionCase);
            break;
          case DISABLED:
          case DEACTIVATED:
            try {
              actionService.cancelActions(caseId);
            } catch (CTPException e) {
              // TODO CTPA-1340 Do we really want to catch this. Should be let to go through.
              // TODO CTPA-1340 What happens with other notif?
            }
            actionCaseRepo.delete(actionCase);
            break;
          default:
            log.warn("Unknown Case lifecycle event {}", notif.getNotificationType());
            break;
        }
      } else {
        log.warn("Cannot accept CaseNotification for none existent actionplan {}", notif.getActionPlanId());
      }
    });

    actionCaseRepo.flush();
  }

  /**
   * This method is to retrive the survey start date from the collection excerise
   * @param notification CaseNotification containing caseId
   * @return CollectionExercise collectionExerciseDTO
   */
  private CollectionExerciseDTO getCollectionExercise(CaseNotification notification) {

   CaseDetailsDTO caseDTO = caseSvcClientServiceImpl.getCase(UUID.fromString(notification.getCaseId()));
   CollectionExerciseDTO collectionExercise = collectionSvcClientServiceImpl
       .getCollectionExercise(caseDTO.getCaseGroup().getCollectionExerciseId());
   return collectionExercise;
  }

  /**
   * In the event that the actions service is incorrectly sent a notification that indicates we should create a case
   * for an already existing caseid, quietly error else save it as a new entry.
   * If we were to allow the save to go ahead we would get a JPA exception, which would result in the notification
   * going back to the queue and us retrying again and again
   * @param actionCase the case to check and save
   */
  private void checkAndSaveCase(ActionCase actionCase) {
    if (actionCaseRepo.findById(actionCase.getId()) != null) {
      log.error("CaseNotification illiciting case creation for an existing case id {}", actionCase.getId());
    } else {
      actionCaseRepo.save(actionCase);
    }
  }
}
