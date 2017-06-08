package uk.gov.ons.ctp.response.action.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlan;

import java.util.UUID;

/**
 * JPA Data Repository.
 */
@Repository
public interface ActionPlanRepository extends JpaRepository<ActionPlan, Integer> {

  /**
   * Return all actions for the specified actionTypeName.
   *
   * @param id UUID id for ActionPlan
   * @return ActionPlan returns ActionPlan for associated id
   */
  ActionPlan findById(UUID id);

}
