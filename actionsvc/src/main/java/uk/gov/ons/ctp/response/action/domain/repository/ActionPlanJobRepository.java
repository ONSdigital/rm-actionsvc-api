package uk.gov.ons.ctp.response.action.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.domain.model.ActionPlanJob;

import java.util.List;
import java.util.UUID;

/**
 * JPA Data Repository.
 */
@Repository
public interface ActionPlanJobRepository extends JpaRepository<ActionPlanJob, Integer> {

  /**
   * Retrieve by UUID
   * @param actionPlanJobId the Id of the ActionPlanJob
   * @return the associated job
   */
  ActionPlanJob findById(UUID actionPlanJobId);

  /**
   * Gte the actionplanjobs for an action plan by id
   * @param actionPlanFK the plan id
   * @return the jobs
   */
  List<ActionPlanJob> findByActionPlanFK(Integer actionPlanFK);
}
