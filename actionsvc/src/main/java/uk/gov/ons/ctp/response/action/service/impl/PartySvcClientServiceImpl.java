package uk.gov.ons.ctp.response.action.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.service.PartySvcClientService;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

import java.util.UUID;

/**
 * Impl of the service that centralizes all REST calls to the Party service
 *
 */
@Slf4j
@Service
public class PartySvcClientServiceImpl implements PartySvcClientService {
  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("partySvcClient")
  private RestClient partySvcClient;

  @Override
  public PartyDTO getParty(final String sampleUnitType, final UUID partyId) {
    PartyDTO partyDTO = partySvcClient.getResource(appConfig.getPartySvc().getPartyBySampleUnitTypeAndIdPath(),
            PartyDTO.class, sampleUnitType, partyId);
    log.debug("PARTY GOTTEN: " + partyDTO.toString());
    return partyDTO;
  }
}
