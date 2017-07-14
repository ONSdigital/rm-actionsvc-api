package uk.gov.ons.ctp.response.action.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * Config POJO for action plan exec params
 *
 */
@CoverageIgnore
@Data
public class DataGrid {
  private String address;
  private String password;
  private Integer listTimeToWaitSeconds;
  private Integer listTimeToLiveSeconds;
  private Integer lockTimeToLiveSeconds;
  private Integer reportLockTimeToLiveSeconds;
}
