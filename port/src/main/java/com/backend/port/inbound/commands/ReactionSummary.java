package com.backend.port.inbound.commands;

/**
 * Immutable projection describing the reaction totals for an incident and the caller's reaction.
 *
 * @param incidentId   target incident identifier
 * @param likes        aggregate like count
 * @param dislikes     aggregate dislike count
 * @param reactionType current user's reaction classification
 */
public record ReactionSummary(long incidentId, int likes, int dislikes, ReactionType reactionType) {

  /**
   * Derives a convenience score computed as likes minus dislikes.
   *
   * @return net sentiment score
   */
  public int score() {
    return likes - dislikes;
  }
}
