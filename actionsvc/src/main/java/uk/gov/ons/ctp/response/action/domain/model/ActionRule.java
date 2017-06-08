package uk.gov.ons.ctp.response.action.domain.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "actionrule", schema = "action")
public class ActionRule implements Serializable {

  private static final long serialVersionUID = 4524689072566205066L;

  @Id
  @Column(name = "actionrulepk")
  private Integer actionRulePK;

  @Column(name = "actionplanfk")
  private Integer actionPlanFK;

  private Integer priority;

  @Column(name = "daysoffset")
  private Integer daysOffset;

  @ManyToOne
  @JoinColumn(name = "actiontypefk")
  private ActionType actionType;

  private String name;

  private String description;
}
