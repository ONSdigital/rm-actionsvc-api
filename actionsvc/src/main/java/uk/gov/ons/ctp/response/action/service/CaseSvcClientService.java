package uk.gov.ons.ctp.response.action.service;

import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDetailsDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseEventDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseGroupDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CategoryDTO;

import java.util.List;
import java.util.UUID;

/**
 * A Service which utilises the CaseSvc via RESTful client calls
 *
 */
public interface CaseSvcClientService {
  /**
   * Create and post to Case service a new CaseEvent
   *
   * @param action the action for which we need the event
   * @param actionCategory the category for the event
   * @return the newly created caseeventdto
   */
  CaseEventDTO createNewCaseEvent(Action action, CategoryDTO.CategoryName actionCategory);

  /**
   * Call CaseSvc using REST to get the CaseGroups details MAY throw a
   * RuntimeException if the call fails
   *
   * @param caseGroupId identifies the Case to fetch
   * @return the Case we fetched
   */
  CaseGroupDTO getCaseGroup(UUID caseGroupId);

  /**
   * Call CaseSvc using REST to get the Case details MAY throw a
   * RuntimeException if the call fails
   *
   * @param caseId identifies the Case to fetch
   * @return the Case we fetched
   *
   */
  CaseDetailsDTO getCase(UUID caseId);

  /**
   * Call CaseSvc using REST to get the Case details MAY throw a
   * RuntimeException if the call fails
   *
   * @param caseId identifies the Case to fetch
   * @return the Case we fetched
   *
   */
  CaseDetailsDTO getCaseWithIAC(final UUID caseId);
  
  /**
   * Call CaseSvc using REST to get the CaseEvents for the Case MAY throw a
   * RuntimeException if the call fails
   *
   * @param caseId identifies the Case to fetch events for
   * @return the CaseEvents we found for the case
   */
  List<CaseEventDTO> getCaseEvents(UUID caseId);

}
