package uk.gov.ons.ctp.response.action.service;

import uk.gov.ons.ctp.response.party.representation.PartyDTO;

import java.util.UUID;

/**
 * A Service which utilises the CaseSvc via RESTful client calls
 *
 */
public interface PartySvcClientService {

  /**
   * Call PartySvc using REST to get the Party MAY throw a RuntimeException if
   * the call fails
   *
   * @param partyId the PartySvc UUID
   * @return the Party we fetched!
   */
  PartyDTO getParty(String sampleUnitType, UUID partyId);

}
