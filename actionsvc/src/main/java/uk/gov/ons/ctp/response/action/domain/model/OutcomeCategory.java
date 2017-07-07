package uk.gov.ons.ctp.response.action.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Domain model object.
 */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "outcomecategory", schema = "action")
public class OutcomeCategory {

  @EmbeddedId
  private OutcomeHandlerId id;

  @Column(name = "eventcategory")
  private String eventCategory;

}
