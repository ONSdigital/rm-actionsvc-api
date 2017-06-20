package uk.gov.ons.ctp.response.action.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.config.CaseSvc;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.domain.model.ActionType;
import uk.gov.ons.ctp.response.casesvc.representation.CaseEventDTO;
import uk.gov.ons.ctp.response.casesvc.representation.CategoryDTO;

import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * A test of the case frame service client service
 */
@RunWith(MockitoJUnitRunner.class)
public class CaseSvcClientServiceImplTest {

  @Mock
  private Tracer tracer;

  @Mock
  private Span span;

  @Mock
  private AppConfig appConfig;

  @Spy
  private RestClient restClient = new RestClient();

  @InjectMocks
  private CaseSvcClientServiceImpl caseSvcClientService;

  
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(tracer.getCurrentSpan()).thenReturn(span);
    Mockito.when(tracer.createSpan(any(String.class))).thenReturn(span);
    restClient.setTracer(tracer);
  }

  /**
   * Yep - another test
   */
  @Test
  public void testCreateNewCaseEvent() {
    CaseSvc caseSvcConfig = new CaseSvc();
    caseSvcConfig.setCaseEventsByCasePostPath("cases/{caseid}/events");
    Mockito.when(appConfig.getCaseSvc()).thenReturn(caseSvcConfig);
    RestTemplate restTemplate = this.restClient.getRestTemplate();

    Action action = new Action();
    action.setId(UUID.fromString("774afa97-8c87-4131-923b-b33ccbf72b3e"));
    action.setActionPlanFK(2);
    action.setActionRuleFK(3);
    
    ActionType actionType = new ActionType();
    actionType.setActionTypePK(4);
    actionType.setHandler("Field");
    actionType.setName("HouseholdVisit");
    actionType.setDescription("desc");
    action.setActionType(actionType);
    action.setCreatedBy("me");
    action.setCaseFK(5);
    action.setCaseId(UUID.fromString("7fac359e-645b-487e-bb02-70536eae51d4"));
    action.setSituation("situ");

    MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
    mockServer.expect(requestTo("http://localhost:8080/cases/7fac359e-645b-487e-bb02-70536eae51d4/events"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(containsString("\"caseId\":\"" + action.getCaseId() + "\",")))
        .andExpect(content()
            .string(containsString("\"category\":\"" + CategoryDTO.CategoryName.ACTION_COMPLETED.name() + "\"")))
        .andExpect(content().string(containsString("\"subCategory\":\"" + action.getActionType().getName() + "\"")))
        .andExpect(content().string(containsString("\"createdBy\":\"" + action.getCreatedBy() + "\"")))
        .andExpect(content().string(containsString("\"description\":\"" + action.getActionType().getDescription() + " (" + action.getSituation() + ")\"")))
        .andRespond(withSuccess("{"
            + "\"createdDateTime\":1460736159699,"
            + "\"caseId\":\"7fac359e-645b-487e-bb02-70536eae51d4\","
            + "\"category\":\"ACTION_COMPLETED\","
            + "\"subCategory\":\"subcat\","
            + "\"createdBy\":\"me\","
            + "\"description\":\"desc\""
            + "}", MediaType.APPLICATION_JSON));

    CaseEventDTO caseEventDTO = caseSvcClientService.createNewCaseEvent(action,
        CategoryDTO.CategoryName.ACTION_COMPLETED);
    assertTrue(caseEventDTO != null);
    mockServer.verify();
  }

}
