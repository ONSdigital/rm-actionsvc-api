package uk.gov.ons.ctp.response.action.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.Procedure;
import uk.gov.ons.ctp.response.action.domain.model.ActionReport;

import javax.transaction.Transactional;
import java.util.UUID;

/**
 * The repository used to trigger stored procedure execution.
 */
public interface ActionReportRepository extends JpaRepository<ActionReport, UUID> {

    /**
     * To execute generate_action_mi
     *
     * @return boolean whether report has been created successfully
     */
    @Modifying
    @Transactional
    @Procedure(name = "ActionReport.mi")
    Boolean miStoredProcedure();
}
