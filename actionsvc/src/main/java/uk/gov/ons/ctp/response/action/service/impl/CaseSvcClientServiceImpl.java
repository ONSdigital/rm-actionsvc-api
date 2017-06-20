package uk.gov.ons.ctp.response.action.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.service.CaseSvcClientService;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDetailsDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseEventDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CaseGroupDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CategoryDTO;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Impl of the service that centralizes all REST calls to the Case service
 *
 */
@Slf4j
@Service
public class CaseSvcClientServiceImpl implements CaseSvcClientService {
  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("caseSvcClient")
  private RestClient caseSvcClient;

  @Override
  public CaseDetailsDTO getCase(final UUID caseId) {
    CaseDetailsDTO caseDTO = caseSvcClient.getResource(appConfig.getCaseSvc().getCaseByCaseGetPath(),
        CaseDetailsDTO.class, caseId);
    return caseDTO;
  }

  @Override
  public CaseGroupDTO getCaseGroup(final UUID caseGroupId) {
    CaseGroupDTO caseGroupDTO = caseSvcClient.getResource(appConfig.getCaseSvc().getCaseGroupPath(),
        CaseGroupDTO.class, caseGroupId);
    return caseGroupDTO;
  }

  @Override
  public List<CaseEventDTO> getCaseEvents(final UUID caseId) {
    List<CaseEventDTO> caseEventDTOs = caseSvcClient.getResources(
        appConfig.getCaseSvc().getCaseEventsByCaseGetPath(),
        CaseEventDTO[].class, caseId);
    return caseEventDTOs;
  }

  @Override
  public CaseEventDTO createNewCaseEvent(final Action action, CategoryDTO.CategoryName actionCategory) {
    log.debug("posting caseEvent for actionId {} to casesvc for category {} ", action.getId(),
        actionCategory);
    CaseEventDTO caseEventDTO = new CaseEventDTO();
    caseEventDTO.setCategory(actionCategory);
    caseEventDTO.setCreatedBy(action.getCreatedBy());
    caseEventDTO.setCreatedDateTime(new Date());
    caseEventDTO.setSubCategory(action.getActionType().getName());

    if (!StringUtils.isEmpty(action.getSituation())) {
      caseEventDTO.setDescription(String.format("%s (%s)",
          action.getActionType().getDescription(), action.getSituation()));
    } else {
      caseEventDTO.setDescription(action.getActionType().getDescription());
    }

    CaseEventDTO returnedCaseEventDTO = caseSvcClient.postResource(
        appConfig.getCaseSvc().getCaseEventsByCasePostPath(), caseEventDTO,
        CaseEventDTO.class,
        action.getCaseId());
    return returnedCaseEventDTO;
  }

@Override
public CaseDetailsDTO getCaseWithIAC(UUID caseId) {
	MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
	queryParams.add("iac", "true");
	CaseDetailsDTO caseDTO = caseSvcClient.getResource(appConfig.getCaseSvc().getCaseByCaseGetPath(),
	    CaseDetailsDTO.class, null, queryParams, caseId);
    return caseDTO;
}

}
