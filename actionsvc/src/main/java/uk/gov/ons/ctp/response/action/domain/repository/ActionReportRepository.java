package uk.gov.ons.ctp.response.action.domain.repository;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ctp.response.action.domain.model.ActionReport;

/**
 * The repository used to trigger stored procedure execution.
 */
public interface ActionReportRepository extends CrudRepository<ActionReport, Long> {

    // TODO CTPA-1409
    @Procedure(name = "CaseReport.plus1inout")
    Integer plus1inoutStoredProcedure(@Param("arg") Integer arg);
}
