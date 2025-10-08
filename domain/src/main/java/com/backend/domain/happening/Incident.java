package com.backend.domain.happening;

import com.backend.domain.actor.ActorId;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.mixins.Expirable;
import com.backend.domain.reactions.EngagementStats;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class Incident extends Happening implements Expirable {

  private static final Duration EXTENSION = Duration.ofMinutes(5);

  @Builder.Default
  @Getter
  private EngagementStats engagementStats = new EngagementStats(0, 0, 0);

  @Builder.Default
  @Getter
  private Instant expiresAt = Instant.now().plus(TTL);

  public Incident(
          Long id,
          @NonNull ActorId actorId,
          @NonNull LocationId locationId,
          String title,
          String description,
          @NonNull Set<Media> media) {

    super(id, actorId, locationId, media, title, description);
    this.engagementStats = new EngagementStats(0, 0, 0);
    this.expiresAt = Expirable.super.expiresAt();
  }

  // Add getter for ID
  public Long getId() {
    return this.id;
  }

  public boolean isDeleted() {
    return engagementStats.consecutiveDenies() >= 3 || isExpired();
  }

  public synchronized void confirmIncident() {
    if (Instant.now().isAfter(expiresAt)) return;

    Instant maxExpiry = createdAt().plus(TTL);
    Instant newExpiry = expiresAt().plus(EXTENSION);

    if (newExpiry.isAfter(maxExpiry)) newExpiry = maxExpiry;

    engagementStats = engagementStats.addConfirm();
    this.overrideExpiresAt(newExpiry);
  }

  public synchronized void denyIncident() {
    engagementStats = engagementStats.addDeny();
  }

  @Override
  public Instant expiresAt() {
    return expiresAt;
  }

  private void overrideExpiresAt(Instant newExpiry) {
    this.expiresAt = newExpiry;
  }
}