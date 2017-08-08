package uk.gov.ons.ctp.response.action.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;


/**
 * Domain entity representing the report table
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "report", schema = "action")
@NamedStoredProcedureQuery(name = "ActionReport.mi",
                procedureName = "action.generate_action_mi",
                parameters = {@StoredProcedureParameter(mode = ParameterMode.OUT, type = Boolean.class)})
public class ActionReport {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "reportPK")
    private Integer reportPK;

    @Column(name = "reporttypeFK")
    private String reportTypeFK;

    @Column(name = "contents")
    private String contents;

    @Column(name = "createddatetime")
    private Timestamp createdDateTime;
}
