package uk.gov.ons.ctp.response.action.service;

import java.util.UUID;

import uk.gov.ons.ctp.common.service.CTPService;
import uk.gov.ons.ctp.response.action.domain.model.ActionCase;

/**
 * The service for ActionPlanJobs
 */
public interface ActionCaseService extends CTPService {
  /**
   * Find the action service case entry for the given id
   * @param caseId the given id
   * @return the case
   */
  ActionCase findActionCase(UUID caseId);
}
