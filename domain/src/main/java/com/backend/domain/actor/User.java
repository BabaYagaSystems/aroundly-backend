package com.backend.domain.actor;

import lombok.Builder;
import lombok.NonNull;

/**
 * Represents authenticated user information extracted from Firebase token.
 * This is a domain object that contains the essential user data.
 */
@Builder
public record User(
  @NonNull UserId uid,
  String email,
  String name,
  String picture,
  boolean emailVerified) {

}