package uk.gov.ons.ctp.response.action.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * Config POJO for distribition params
 *
 */
@CoverageIgnore
@Data
public class ActionDistribution {
  private Integer retrievalMax;
  private Integer retrySleepSeconds;
  private Integer delayMilliSeconds;
}
