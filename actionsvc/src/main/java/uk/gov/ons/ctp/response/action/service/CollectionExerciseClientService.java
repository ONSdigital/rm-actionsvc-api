package uk.gov.ons.ctp.response.action.service;

import java.util.UUID;

import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

/**
 * A Service which utilises the CollectionexerciseSvc via RESTful client calls
 *
 */
public interface CollectionExerciseClientService {

	CollectionExerciseDTO getCollectionExercise(UUID collectionId);
	
}
