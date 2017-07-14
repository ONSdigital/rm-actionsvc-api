package uk.gov.ons.ctp.response.action.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * Config POJO for Reports
 */
@CoverageIgnore
@Data
public class ReportSettings {
    private String cronExpression;
}
