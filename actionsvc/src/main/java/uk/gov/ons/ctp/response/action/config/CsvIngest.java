package uk.gov.ons.ctp.response.action.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * App config POJO for csv ingest params
 *
 */
@CoverageIgnore
@Data
public class CsvIngest {
  private String directory;
  private String filePattern;
  private Integer pollMilliseconds;
}
