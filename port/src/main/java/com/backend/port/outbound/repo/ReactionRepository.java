package com.backend.port.outbound.repo;

import com.backend.port.inbound.commands.ReactionSummary;

/**
 * Outbound port describing the operations needed to persist and retrieve incident reactions.
 * Implementations decide how to store and synchronise likes/dislikes (e.g. Redis + Postgres).
 */
public interface ReactionRepository {

  /**
   * Registers (or toggles to) a like reaction for the given incident and user.
   *
   * @param incidentId incident identifier
   * @param userId     unique user identifier
   * @return updated aggregate reaction summary
   */
  ReactionSummary addLike(long incidentId, String userId);

  /**
   * Registers (or toggles to) a dislike reaction for the given incident and user.
   *
   * @param incidentId incident identifier
   * @param userId     unique user identifier
   * @return updated aggregate reaction summary
   */
  ReactionSummary addDislike(long incidentId, String userId);

  /**
   * Removes an existing like reaction for the given incident and user.
   *
   * @param incidentId incident identifier
   * @param userId     unique user identifier
   * @return updated aggregate reaction summary
   */
  ReactionSummary removeLike(long incidentId, String userId);

  /**
   * Removes an existing dislike reaction for the given incident and user.
   *
   * @param incidentId incident identifier
   * @param userId     unique user identifier
   * @return updated aggregate reaction summary
   */
  ReactionSummary removeDislike(long incidentId, String userId);

  /**
   * Retrieves the current reaction summary for the given incident, optionally tailored to the
   * supplied user (to indicate their current reaction).
   *
   * @param incidentId incident identifier
   * @param userId     unique user identifier (may be blank for anonymous lookups)
   * @return aggregate reaction summary
   */
  ReactionSummary getSummary(long incidentId, String userId);
}
