package com.backend.services;

import com.backend.port.inbound.ReactionUseCase;
import com.backend.port.inbound.commands.ReactToIncidentCommand;
import com.backend.port.inbound.commands.ReactionSummary;
import com.backend.port.outbound.repo.IncidentRepository;
import com.backend.port.outbound.repo.ReactionRepository;
import com.backend.services.exceptions.IncidentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating like/dislike operations for incidents. It guards the use case
 * against invalid incident IDs and delegates persistence details to the outbound reaction port.
 */
@Service
@RequiredArgsConstructor
public class ReactionService implements ReactionUseCase {

  private final ReactionRepository reactionRepository;
  private final IncidentRepository incidentRepository;

  /**
   * Adds (or toggles to) a like reaction for the current user and incident.
   *
   * @param command incoming reaction command containing incident and user identifiers
   * @return updated reaction summary
   */
  @Override
  @Transactional
  public ReactionSummary addLike(ReactToIncidentCommand command) {
    ensureIncidentExists(command.incidentId());
    return reactionRepository.addLike(command.incidentId(), command.userId());
  }

  /**
   * Adds (or toggles to) a dislike reaction for the current user and incident.
   *
   * @param command incoming reaction command containing incident and user identifiers
   * @return updated reaction summary
   */
  @Override
  @Transactional
  public ReactionSummary addDislike(ReactToIncidentCommand command) {
    ensureIncidentExists(command.incidentId());
    return reactionRepository.addDislike(command.incidentId(), command.userId());
  }

  /**
   * Removes a previously registered like reaction for the current user and incident.
   *
   * @param command incoming reaction command containing incident and user identifiers
   * @return updated reaction summary
   */
  @Override
  @Transactional
  public ReactionSummary removeLike(ReactToIncidentCommand command) {
    ensureIncidentExists(command.incidentId());
    return reactionRepository.removeLike(command.incidentId(), command.userId());
  }

  /**
   * Removes a previously registered dislike reaction for the current user and incident.
   *
   * @param command incoming reaction command containing incident and user identifiers
   * @return updated reaction summary
   */
  @Override
  @Transactional
  public ReactionSummary removeDislike(ReactToIncidentCommand command) {
    ensureIncidentExists(command.incidentId());
    return reactionRepository.removeDislike(command.incidentId(), command.userId());
  }

  /**
   * Reads the current reaction summary for the incident, taking the user's reaction (if any) into
   * account.
   *
   * @param command incoming reaction command containing incident and user identifiers
   * @return current reaction summary without mutating state
   */
  @Override
  @Transactional
  public ReactionSummary getSummary(ReactToIncidentCommand command) {
    ensureIncidentExists(command.incidentId());
    return reactionRepository.getSummary(command.incidentId(), command.safeUserId());
  }

  /**
   * Verifies that the incident exists before invoking the reaction repository.
   *
   * @param incidentId incident identifier to validate
   */
  private void ensureIncidentExists(long incidentId) {
    if (!incidentRepository.existsById(incidentId)) {
      throw new IncidentNotFoundException("Incident not found with id: " + incidentId);
    }
  }

  /**
   * Ensures a user identifier is present before performing mutations.
   *
   * @param command reaction command carrying the user context
   */
  private void assertUser(ReactToIncidentCommand command) {
    if (!command.hasUserContext()) {
      throw new IllegalArgumentException("User id is required to mutate reactions");
    }
  }
}
