package com.backend.services;

import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import com.backend.port.inbound.UserUseCase;
import com.backend.port.inbound.commands.UpdateDeviceIdTokenCommand;
import com.backend.port.inbound.commands.UpdateRangeCommand;
import com.backend.port.outbound.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates synchronization between Firebase-authenticated users and the local persistence
 * layer. Responsibilities include idempotent creation, profile updates, preference management,
 * and exposing the currently authenticated principal to the rest of the application.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository userRepository;

  /**
   * Idempotently persists information extracted from a Firebase token.
   * If the user already exists, only mutable attributes (email/name/picture)
   * are refreshed; otherwise a new record is created.
   *
   * @param user domain user hydrated from the authentication token
   * @return optional containing the stored user, or empty when persistence fails
   */
  @Override
  @Transactional
  public Optional<User> create(final User user) {
    if (userRepository.existsByFirebaseUid(user.uid().value())) {
      return userRepository.findByFirebaseUid(user.uid().value())
          .map(existing -> {
            User mergedUser = existing.toBuilder()
                .email(user.email())
                .name(user.name())
                .picture(user.picture())
                .build();
            return userRepository.save(mergedUser);
          });
    }
    return Optional.ofNullable(userRepository.save(user));
  }

  /**
   * Applies user-driven profile updates such as role or picture changes.
   *
   * @param userId Firebase identifier of the user to change
   * @param updatedUser payload containing new values
   * @return updated user when found, otherwise empty
   */
  @Override
  @Transactional
  public Optional<User> update(final UserId userId, final User updatedUser) {
    final Optional<User> existingUser = userRepository.findByFirebaseUid(userId.value());

    if (existingUser.isPresent()) {
      final User updatedChanges = existingUser.get()
          .toBuilder()
          .email(updatedUser.email())
          .name(updatedUser.name())
          .picture(updatedUser.picture())
          .role(updatedUser.role())
          .build();

      return Optional.ofNullable(userRepository.save(updatedChanges));
    }

    log.error("Cound not upated user: {}", userId.value());
    return Optional.empty();
  }

  /**
   * Resolves the principal currently held by Spring Security for the active request.
   *
   * @return authenticated user if available, otherwise empty
   */
  @Override
  @Transactional
  public Optional<User> getUser() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof User userInfo) {
      return Optional.of(userInfo);
    }
    log.error("No user were found.");

    return Optional.empty();
  }

  /**
   * @return true when {@link #getUser()} yields a principal, false when no user is authenticated
   */
  @Override
  @Transactional
  public boolean isAuthenticated() {
    return getUser().isPresent();
  }

  /**
   * Stores or refreshes the user's FCM token/device identifier so push notifications
   * can be delivered to the correct device.
   *
   * @param updateDeviceIdTokenCommand command containing Firebase uid and the latest token
   * @return updated user if the uid exists, empty otherwise
   */
  @Override
  @Transactional
  public Optional<User> updateDeviceIdToken(final UpdateDeviceIdTokenCommand updateDeviceIdTokenCommand) {
    final String firebaseUid = updateDeviceIdTokenCommand.firebaseUid();
    final String deviceIdToken = updateDeviceIdTokenCommand.deviceIdToken();

    final Optional<User> user = userRepository.findByFirebaseUid(firebaseUid);

    if (user.isPresent()) {
      final User updatedUser = user.get().toBuilder()
          .deviceIdToken(deviceIdToken)
          .build();

      return Optional.ofNullable(userRepository.save(updatedUser));
    }

    log.error("Could not update the device id token for user: {}", firebaseUid);
    return Optional.empty();
  }

  /**
   * Saves the preferred notification radius for incident filtering.
   *
   * @param updateRangeCommand command holding Firebase uid and desired range in km
   * @return updated user when the uid exists, empty otherwise
   */
  @Override
  @Transactional
  public Optional<User> updateRange(final UpdateRangeCommand updateRangeCommand) {
    final String firebaseUid = updateRangeCommand.firebaseUid();
    final int range = updateRangeCommand.rangeKm();
    final Optional<User> user = userRepository.findByFirebaseUid(firebaseUid);

    if (user.isPresent()) {
      final User updatedUser = user.get().toBuilder()
          .range(range)
          .build();

      return Optional.ofNullable(userRepository.save(updatedUser));
    }

    log.error("Could not update the range for user: {}", firebaseUid);
    return Optional.empty();
  }
}
