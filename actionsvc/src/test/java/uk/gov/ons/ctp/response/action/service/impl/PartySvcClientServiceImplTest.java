package uk.gov.ons.ctp.response.action.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.config.PartySvc;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

/**
 * test for PartySvcClient
 */
@RunWith(MockitoJUnitRunner.class)
public class PartySvcClientServiceImplTest {

    @InjectMocks
    private PartySvcClientServiceImpl partySvcClientService;

    @Mock
    private RestClient partySvcClient;

    @Mock
    private AppConfig appConfig;

    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyGetPartyCallInvokesRESTEndpoint() throws CTPException {
        PartyDTO party = new PartyDTO();

        PartySvc partySvc = new PartySvc();
        partySvc.setPartyBySampleUnitTypeAndIdPath("/sampleUnit");

        when(appConfig.getPartySvc()).thenReturn(partySvc);
        when(partySvcClient.getResource("/sampleUnit", PartyDTO.class, "B", UUID.fromString("d06c440e-4fad-4ea6-952a-72d9db144f05"))).thenReturn(party);

        partySvcClientService.getParty("B", UUID.fromString("d06c440e-4fad-4ea6-952a-72d9db144f05"));

        verify(partySvcClient, times(1)).getResource("/sampleUnit", PartyDTO.class, "B", UUID.fromString("d06c440e-4fad-4ea6-952a-72d9db144f05"));
    }
}