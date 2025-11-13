package com.backend.port.inbound;

import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import com.backend.port.inbound.commands.UpdateDeviceIdTokenCommand;
import com.backend.port.inbound.commands.UpdateRangeCommand;
import java.util.Optional;

/**
 * Defines the user-facing use cases exposed to inbound adapters.
 * Implementations are responsible for keeping Firebase-authenticated users
 * synchronized with the local persistence layer, exposing the currently
 * authenticated user, and maintaining user preferences such as push tokens
 * and notification range.
 */
public interface UserUseCase {

  /**
   * Creates a new user entry or ensures the Firebase-authenticated user
   * exists in the local datastore.
   *
   * @param user domain representation extracted from Firebase token
   * @return optional containing the stored user, or empty if the operation fails
   */
  Optional<User> create(User user);

  /**
   * Updates mutable attributes (e-mail, display name, role, etc.) of an existing user.
   *
   * @param userId unique Firebase-based identifier
   * @param user payload carrying the new values
   * @return optional with the updated user if present
   */
  Optional<User> update(UserId userId, User user);

  /**
   * Retrieves the user currently authorized in the running request context.
   *
   * @return optional containing the authenticated user, or empty if no
   *         security context is established
   */
  Optional<User> getUser();

  /**
   * Convenience method signalling whether the current request is authenticated.
   *
   * @return true when {@link #getUser()} resolves to a user, false otherwise
   */
  boolean isAuthenticated();

  /**
   * Persists the latest Firebase Cloud Messaging token or device identifier
   * for the authenticated user so push notifications can be delivered.
   *
   * @param updateDeviceIdTokenCommand command wrapping the uid and token
   * @return optional with the updated user if the uid exists
   */
  Optional<User> updateDeviceIdToken(UpdateDeviceIdTokenCommand updateDeviceIdTokenCommand);

  /**
   * Records the preferred radius (in kilometers) used to filter nearby incidents.
   *
   * @param updateRangeCommand command carrying the uid and new range value
   * @return optional with the updated user if the uid exists
   */
  Optional<User> updateRange(UpdateRangeCommand updateRangeCommand);
}
