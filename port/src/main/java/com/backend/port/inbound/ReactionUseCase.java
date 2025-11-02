package com.backend.port.inbound;

import com.backend.port.inbound.commands.ReactToIncidentCommand;
import com.backend.port.inbound.commands.ReactionSummary;

/**
 * Defines use cases for managing reactions such as likes and dislikes.
 *
 * Each method returns a {@link ReactionSummary} reflecting the updated
 * state of reactions after the operation is applied.
 */
public interface ReactionUseCase {

  /**
   * Adds a like reaction.
   *
   * @return the updated reaction summary
   */
  ReactionSummary addLike(ReactToIncidentCommand command);

  /**
   * Adds a dislike reaction.
   *
   * @return the updated reaction summary
   */
  ReactionSummary addDislike(ReactToIncidentCommand command);

  /**
   * Removes a like reaction.
   *
   * @return the updated reaction summary
   */
  ReactionSummary removeLike(ReactToIncidentCommand command);

  /**
   * Removes a dislike reaction.
   *
   * @return the updated reaction summary
   */
  ReactionSummary removeDislike(ReactToIncidentCommand command);

  /**
   * Retrieves the reaction summary.
   *
   * @return the reaction summary
   */
  ReactionSummary getSummary(ReactToIncidentCommand command);
}
