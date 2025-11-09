package com.backend.port.inbound;

import com.backend.domain.actor.UserId;
import com.backend.domain.actor.Role;
import java.util.Optional;
import java.util.Set;

/**
 * Defines use cases for working with the current authenticated Actor.
 *
 * Provides methods to extract actor-related information such as identifier,
 * roles, username, and avatar URL.
 */
public interface ActorUseCase {

  /**
   * Extracts the unique identifier of the current actor.
   *
   * @return the actor's identifier
   */
  UserId extractId();

  /**
   * Extracts the set of roles assigned to the current actor.
   *
   * @return the actor's roles
   */
  Set<Role> extractRoles();

  /**
   * Extracts the username of the current actor, if available.
   *
   * @return an {@link Optional} containing the username, or empty if not available
   */
  Optional<String> extractUsername();

  /**
   * Extracts the avatar URL of the current actor, if available.
   *
   * @return an {@link Optional} containing the avatar URL, or empty if not available
   */
  Optional<String> extractAvatarUrl();
}
