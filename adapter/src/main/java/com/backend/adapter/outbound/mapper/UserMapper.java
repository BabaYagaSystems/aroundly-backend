package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import java.time.Instant;

/**
 * Utility that converts between domain {@link User} aggregates and the JPA {@link UserEntity}.
 * Handles defaults such as timestamps so the persistence layer does not leak into higher layers.
 */
public final class UserMapper {

  /**
   * Maps the domain model into a persistence entity, applying default values for fields that
   * are otherwise managed inside the database.
   */
  public static UserEntity mapToEntity(final User user) {

    return UserEntity.builder()
        .firebaseUid(user.uid().value())
        .email(user.email())
        .displayName(user.name())
        .pictureUrl(user.picture())
        .fcmToken(user.deviceIdToken())
        .rangeKm(user.range())
        .role(user.role())
        .isActive(true)
        .createdAt(Instant.now())
        .lastLogin(Instant.now())
        .build();
  }

  /**
   * Maps a persistence entity back into the domain model consumed by services and ports.
   */
  public static User mapToDomain(final UserEntity entity) {
    return User.builder()
        .uid(new UserId(entity.getFirebaseUid()))
        .email(entity.getEmail())
        .name(entity.getDisplayName())
        .picture(entity.getPictureUrl())
        .role(entity.getRole())
        .deviceIdToken(entity.getFcmToken())
        .range(entity.getRangeKm())
        .build();
  }

}
