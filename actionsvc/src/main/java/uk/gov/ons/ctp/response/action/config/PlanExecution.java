package uk.gov.ons.ctp.response.action.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * Config POJO for action plan exec params
 *
 */
@CoverageIgnore
@Data
public class PlanExecution {
  private Long delayMilliSeconds;
}
