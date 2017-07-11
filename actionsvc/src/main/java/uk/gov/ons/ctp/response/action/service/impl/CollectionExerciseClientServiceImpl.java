package uk.gov.ons.ctp.response.action.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.action.config.AppConfig;
import uk.gov.ons.ctp.response.action.service.CollectionExerciseClientService;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

/**
 * Impl of the service that centralizes all REST calls to the Collection Exercise service
 */
@Service
public class CollectionExerciseClientServiceImpl implements CollectionExerciseClientService {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("collectionExerciseSvcClient")
  private RestClient collectionExceriseSvcClient;

  @Override
  public CollectionExerciseDTO getCollectionExercise(UUID collectionExcerciseId) {
    CollectionExerciseDTO collectionDTO = collectionExceriseSvcClient
            .getResource(appConfig.getCollectionExerciseSvc().getCollectionByCollectionExerciseGetPath(),
        CollectionExerciseDTO.class, collectionExcerciseId);
    return collectionDTO;
  }

}
