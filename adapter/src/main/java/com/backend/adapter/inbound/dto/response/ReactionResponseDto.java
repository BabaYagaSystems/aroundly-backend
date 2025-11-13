package com.backend.adapter.inbound.dto.response;

/**
 * REST response payload exposing the aggregate reaction state for a specific incident.
 *
 * @param incidentId incident identifier the reactions belong to
 * @param likes      total like count
 * @param dislikes   total dislike count
 * @param score      convenience metric computed as likes minus dislikes
 */
public record ReactionResponseDto(
    long incidentId,
    int likes,
    int dislikes,
    int score) { }
