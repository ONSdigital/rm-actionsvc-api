package uk.gov.ons.ctp.response.action.config;

import lombok.Data;
import uk.gov.ons.ctp.common.rest.RestClientConfig;

/**
 * App config POJO for collection excerise service access - host/location and endpoint
 * locations
 *
 */
@Data
public class CollectionExerciseSvc {
  private RestClientConfig connectionConfig;
  private String collectionByCollectionExerciseGetPath;

}
