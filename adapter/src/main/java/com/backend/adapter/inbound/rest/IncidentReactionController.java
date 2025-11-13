package com.backend.adapter.inbound.rest;

import static com.backend.adapter.inbound.mapper.ReactionSummaryMapper.mapToReactionResponseDto;

import com.backend.adapter.inbound.dto.response.ReactionResponseDto;
import com.backend.port.inbound.ReactionUseCase;
import com.backend.port.inbound.commands.ReactToIncidentCommand;
import com.backend.port.inbound.commands.ReactionSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing like/dislike operations for incidents, delegating
 * the business logic to the reaction use case and mapping results into API DTOs.
 */
@RestController
@RequestMapping("/api/v1/incidents/{incidentId}/reactions")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "ReactionEntity manipulation endpoints")
public class IncidentReactionController {

  private final ReactionUseCase reactionUseCase;

  /**
   * Registers a like reaction for the given incident and returns the updated reaction summary.
   *
   * @param incidentId identifier of the incident to like
   */
  @PostMapping("/like")
  @Operation(
      summary = "Registers a like reaction",
      description = "Adds a like for the specified incident and returns refreshed engagement counters."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Reaction registered successfully"),
      @ApiResponse(responseCode = "404", description = "Incident not found")
  })
  public ResponseEntity<ReactionResponseDto> likeIncident(@PathVariable long incidentId) {
    ReactionSummary summary = reactionUseCase.addLike(new ReactToIncidentCommand(incidentId, null));

    return ResponseEntity.status(HttpStatus.CREATED).body(mapToReactionResponseDto(summary));
  }

  /**
   * Registers a dislike reaction for the given incident and returns the updated reaction summary.
   *
   * @param incidentId identifier of the incident to dislike
   */
  @PostMapping("/dislike")
  @Operation(
      summary = "Registers a dislike reaction",
      description = "Adds a dislike for the specified incident and returns refreshed engagement counters."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Reaction registered successfully"),
      @ApiResponse(responseCode = "404", description = "Incident not found")
  })
  public ResponseEntity<ReactionResponseDto> dislikeIncident(@PathVariable long incidentId) {
    ReactionSummary summary = reactionUseCase.addDislike(new ReactToIncidentCommand(incidentId, null));

    return ResponseEntity.status(HttpStatus.CREATED).body(mapToReactionResponseDto(summary));
  }

  /**
   * Removes a like reaction from the given incident and returns the updated reaction summary.
   *
   * @param incidentId identifier of the incident whose like is being removed
   */
  @DeleteMapping("/like")
  @Operation(
      summary = "Removes a like reaction",
      description = "Deletes the like for the specified incident and returns refreshed engagement counters."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Reaction removed successfully"),
      @ApiResponse(responseCode = "404", description = "Incident not found")
  })
  public ResponseEntity<ReactionResponseDto> removeLike(@PathVariable long incidentId) {
    ReactionSummary summary = reactionUseCase.removeLike(new ReactToIncidentCommand(incidentId, null));

    return ResponseEntity.ok(mapToReactionResponseDto(summary));
  }

  /**
   * Removes a dislike reaction from the given incident and returns the updated reaction summary.
   *
   * @param incidentId identifier of the incident whose dislike is being removed
   */
  @DeleteMapping("/dislike")
  @Operation(
      summary = "Removes a dislike reaction",
      description = "Deletes the dislike for the specified incident and returns refreshed engagement counters."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Reaction removed successfully"),
      @ApiResponse(responseCode = "404", description = "Incident not found")
  })
  public ResponseEntity<ReactionResponseDto> removeDislike(@PathVariable long incidentId) {
    ReactionSummary summary = reactionUseCase.removeDislike(new ReactToIncidentCommand(incidentId, null));

    return ResponseEntity.ok(mapToReactionResponseDto(summary));
  }

  /**
   * Retrieves the current reaction summary (likes/dislikes) for the given incident.
   *
   * @param incidentId identifier of the incident for which to fetch reactions
   */
  @GetMapping
  @Operation(
      summary = "Fetches reaction summary",
      description = "Returns the current like/dislike totals and net score for the specified incident."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Reaction summary retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Incident not found")
  })
  public ResponseEntity<ReactionResponseDto> getSummary(@PathVariable long incidentId) {
    ReactionSummary summary = reactionUseCase.getSummary(new ReactToIncidentCommand(incidentId, null));

    return ResponseEntity.ok(mapToReactionResponseDto(summary));
  }
}
