package com.backend.port.outbound.repo;

import com.backend.domain.actor.User;
import java.util.Optional;

/**
 * Outbound port that encapsulates persistence operations required by user-facing use cases.
 * Implementations (e.g., JPA repositories) must ensure Firebase users are stored and retrieved
 * using the Firebase UID as the canonical identifier while exposing convenient lookup helpers.
 */
public interface UserRepository {

  /**
   * Persists the supplied user, either by inserting a new record or updating an existing one.
   *
   * @param user aggregate to store
   * @return the saved user (including any persistence-generated defaults)
   */
  User save(User user);

  /**
   * Locates a user using their Firebase UID, which is the unique identifier across systems.
   *
   * @param firebaseUid Firebase UID to search for
   * @return optional containing the user when found
   */
  Optional<User> findByFirebaseUid(String firebaseUid);

  /**
   * Finds a user by email address when Firebase UID is not available.
   *
   * @param email email address to look up
   * @return optional containing the matching user, if any
   */
  Optional<User> findByEmail(String email);

  /**
   * Checks whether a user already exists for the provided Firebase UID.
   *
   * @param firebaseUid Firebase UID to verify
   * @return true when a record exists, false otherwise
   */
  boolean existsByFirebaseUid(String firebaseUid);
}
