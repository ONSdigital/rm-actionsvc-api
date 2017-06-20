package uk.gov.ons.ctp.response.action.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.domain.model.Action;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * JPA Data Repository.
 */
@Repository
public interface ActionRepository extends JpaRepository<Action, BigInteger> {

  /**
   * Return all actions, most recent first.
   *
   * @return all actions
   */
  List<Action> findAllByOrderByCreatedDateTimeDesc();

  /**
   * Find action by UUID
   * @param actionId the action uuid
   * @return the action found
   */
  Action findById(UUID actionId);

  /**
   * Return all actions for the specified case id.
   *
   * @param caseId This is the case id
   * @return List<Action> This returns all actions for the specified case id.
   */
  List<Action> findByCaseId(UUID caseId);

  /**
   * Return all actions for the specified case id, ordered by created DateTime.
   *
   * @param caseId This is the case id
   * @return List<Action> This returns all actions for the specified case id.
   */
  List<Action> findByCaseIdOrderByCreatedDateTimeDesc(UUID caseId);

  /**
   * Return all actions for the specified actionTypeName and state in created
   * date time order descending.
   *
   * @param actionTypeName ActionTypeName filter criteria
   * @param state State of Action
   * @return List<Action> returns all actions for actionTypeName and state
   */
  List<Action> findByActionTypeNameAndStateOrderByCreatedDateTimeDesc(String actionTypeName,
      ActionDTO.ActionState state);

  /**
   * Return all actions for the specified actionTypeName and states according to
   * the page specification
   *
   * @param actionTypeName ActionTypeName filter criteria
   * @param states States of Action
   * @param actionPKs the actionPKs
   * @param pageable the paging info for the query
   * @return List<Action> returns all actions for actionTypeName and states, for
   *         the given page
   */
  List<Action> findByActionTypeNameAndStateInAndActionPKNotIn(String actionTypeName,
      List<ActionDTO.ActionState> states, List<BigInteger> actionPKs, Pageable pageable);

  /**
   * Return all actions for the specified actionTypeName.
   *
   * @param actionTypeName ActionTypeName filter criteria
   * @return List<Action> returns all actions for actionTypeName
   */
  List<Action> findByActionTypeNameOrderByCreatedDateTimeDesc(String actionTypeName);

  /**
   * Return all actions for the specified state.
   *
   * @param state State filter criteria
   * @return List<Action> returns all actions for state
   */
  List<Action> findByStateOrderByCreatedDateTimeDesc(ActionDTO.ActionState state);

}
