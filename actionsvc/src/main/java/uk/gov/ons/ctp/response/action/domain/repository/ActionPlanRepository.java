package uk.gov.ons.ctp.response.action.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;

/**
 * JPA Data Repository.
 */
@Repository
public interface ActionPlanRepository extends JpaRepository<ActionPlan, Integer> {

  public ActionPlan findById(UUID id);
}
