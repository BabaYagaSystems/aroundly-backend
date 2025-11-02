package com.backend.port.inbound.commands;

import java.util.Objects;

/**
 * Command encapsulating the context required to apply or inspect a reaction for an incident.
 *
 * @param incidentId target incident identifier (must be positive)
 * @param userId     identifier of the reacting user (may be {@code null} for anonymous lookups)
 */
public record ReactToIncidentCommand(long incidentId, String userId) {

  public ReactToIncidentCommand {
    if (incidentId <= 0) {
      throw new IllegalArgumentException("Incident id must be positive");
    }
  }

  /**
   * Indicates whether the command carries a non-blank user identifier.
   *
   * @return {@code true} when a reacting user is present
   */
  public boolean hasUserContext() {
    return userId != null && !userId.isBlank();
  }

  /**
   * Safely returns the user identifier, defaulting to an empty string when absent.
   *
   * @return user identifier or empty string
   */
  public String safeUserId() {
    return Objects.toString(userId, "");
  }
}
