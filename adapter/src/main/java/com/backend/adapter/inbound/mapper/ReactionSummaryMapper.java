package com.backend.adapter.inbound.mapper;

import com.backend.adapter.inbound.dto.response.ReactionResponseDto;
import com.backend.port.inbound.commands.ReactionSummary;

/**
 * Converts domain-level {@link ReactionSummary} projections into REST-facing DTOs.
 */
public final class ReactionSummaryMapper {

  private ReactionSummaryMapper() {
    // utility class
  }

  /**
   * Maps a {@link ReactionSummary} returned by the application layer into the response payload used
   * by the REST controller.
   *
   * @param reactionSummary domain summary to convert
   * @return DTO containing the same reaction metrics
   */
  public static ReactionResponseDto mapToReactionResponseDto(ReactionSummary reactionSummary) {
    return new ReactionResponseDto(
        reactionSummary.incidentId(),
        reactionSummary.likes(),
        reactionSummary.dislikes(),
        reactionSummary.score());
  }
}
