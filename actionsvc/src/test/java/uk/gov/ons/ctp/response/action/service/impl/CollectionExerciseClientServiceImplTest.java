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
import uk.gov.ons.ctp.response.action.config.CollectionExerciseSvc;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

/**
 * test for CollectionExerciseSvcClient
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionExerciseClientServiceImplTest {

  @InjectMocks
  private CollectionExerciseClientServiceImpl collectionExerciseClientServiceImpl;

  @Mock
  private RestClient collectionExceriseSvcClient;

  @Mock
  private AppConfig appConfig;

  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void verifyGetCollectionExerciseCallInvokesRESTEndpoint() throws CTPException {
    CollectionExerciseDTO collectionDTO = new CollectionExerciseDTO();

    CollectionExerciseSvc collectionExerciseSvc = new CollectionExerciseSvc();
    collectionExerciseSvc.setCollectionByCollectionExerciseGetPath("/path");

    when(appConfig.getCollectionExerciseSvc()).thenReturn(collectionExerciseSvc);
    when(collectionExceriseSvcClient.getResource("/path", CollectionExerciseDTO.class,
        UUID.fromString("d06c440e-4fad-4ea6-952a-72d9db144f05"))).thenReturn(collectionDTO);

    collectionExerciseClientServiceImpl.getCollectionExercise(UUID.fromString("d06c440e-4fad-4ea6-952a-72d9db144f05"));

    verify(collectionExceriseSvcClient, times(1)).getResource("/path", CollectionExerciseDTO.class,
        UUID.fromString("d06c440e-4fad-4ea6-952a-72d9db144f05"));
  }
}
