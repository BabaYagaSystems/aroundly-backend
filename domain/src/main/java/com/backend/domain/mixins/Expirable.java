package com.backend.domain.mixins;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Mixin interface for objects that have an expiration time,
 * defined as 30 minutes after creation.
 */
public interface Expirable extends TimeStamped {

  Duration TTL = Duration.ofMinutes(30);

  /**
   * Returns the expiration time of the object.
   *
   * @return the expiration time, 30 minutes after creation
   */
  default Instant getExpiresAt() {
    return createdAt().plus(TTL);
  }

  /**
   * Checks if the object has expired at the system clock.
   *
   * @return true if expired, false otherwise
   */
  default boolean isExpired() {
    return Instant.now().isAfter(getExpiresAt());
  }

  /**
   * Checks if the object has expired at the given clock. (Test Only!)
   *
   * @param clock the clock to evaluate against
   * @return true if expired, false otherwise
   */
  default boolean isExpired(Clock clock) {
    return Instant.now(clock).isAfter(getExpiresAt());
  }
}
