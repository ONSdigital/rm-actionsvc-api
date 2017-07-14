package uk.gov.ons.ctp.response.action.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * The apps main holder for centralized config read from application.yml or env
 * vars
 *
 */
@CoverageIgnore
@Configuration
@ConfigurationProperties
@Data
public class AppConfig {
  private CaseSvc caseSvc;
  private CollectionExerciseSvc collectionExerciseSvc;
  private PartySvc partySvc;
  private ActionDistribution actionDistribution;
  private PlanExecution planExecution;
  private CsvIngest csvIngest;
  private DataGrid dataGrid;
  private SwaggerSettings swaggerSettings;
  private ReportSettings reportSettings;
}
