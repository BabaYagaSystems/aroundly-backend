package com.backend.adapter.outbound.repo.persistence;

import static com.backend.adapter.outbound.mapper.UserMapper.mapToDomain;
import static com.backend.adapter.outbound.mapper.UserMapper.mapToEntity;

import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.mapper.UserMapper;
import com.backend.adapter.outbound.repo.UserPersistenceRepository;
import com.backend.domain.actor.User;
import com.backend.port.outbound.repo.UserRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Bridges the domain-level {@link UserRepository} contract with the JPA infrastructure.
 * This adapter handles the conversion between {@link User} aggregates and {@link UserEntity}
 * objects while ensuring Firebase users are merged rather than duplicated.
 */
@Repository
@RequiredArgsConstructor
public class UserPersistence implements UserRepository {

  private final UserPersistenceRepository repository;

  /**
   * Persists the provided user, merging with an existing entity when the Firebase UID already
   * exists to keep the operation idempotent.
   */
  @Override
  public User save(final User user) {
    final UserEntity userEntity = repository.findByFirebaseUid(user.uid().value())
        .map(existing -> mergeUser(existing, user))
        .orElseGet(() -> mapToEntity(user));

    final UserEntity savedUserEntity = repository.save(userEntity);
    return mapToDomain(savedUserEntity);
  }

  /**
   * Retrieves a user by Firebase UID.
   *
   * @param firebaseUid unique Firebase identifier
   * @return optional containing the mapped domain user
   */
  @Override
  public Optional<User> findByFirebaseUid(final String firebaseUid) {
    return repository.findByFirebaseUid(firebaseUid)
        .map(UserMapper::mapToDomain);
  }

  /**
   * Retrieves a user by email address.
   *
   * @param email email to look up
   * @return optional containing the mapped domain user
   */
  @Override
  public Optional<User> findByEmail(final String email) {
    return repository.findByEmail(email)
        .map(UserMapper::mapToDomain);
  }

  /**
   * Indicates whether a row already exists for the given Firebase UID.
   */
  @Override
  public boolean existsByFirebaseUid(final String firebaseUid) {
    return repository.existsByFirebaseUid(firebaseUid);
  }

  /**
   * Applies any non-null attributes from the domain model to the existing entity so that display
   * name, email, tokens, and range stay current without losing immutable database fields.
   */
  private UserEntity mergeUser(final UserEntity target, final User source) {
    if (source.email() != null) {
      target.setEmail(source.email());
    }
    if (source.name() != null) {
      target.setDisplayName(source.name());
    }
    if (source.picture() != null) {
      target.setPictureUrl(source.picture());
    }
    if (source.deviceIdToken() != null) {
      target.setFcmToken(source.deviceIdToken());
    }
    if (source.range() > 0) {
      target.setRangeKm(source.range());
    }
    if (source.role() != null) {
      target.setRole(source.role());
    }

    target.setLastLogin(Instant.now());
    return target;
  }
}
