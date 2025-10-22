package com.backend.domain.happening;

import com.backend.domain.actor.ActorId;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.mixins.Actored;
import com.backend.domain.mixins.Expirable;
import com.backend.domain.mixins.HasMedia;
import com.backend.domain.mixins.Locatable;
import com.backend.domain.mixins.Reactable;
import com.backend.domain.reactions.EngagementStats;
import com.backend.domain.reactions.SentimentEngagement;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents an {@link Incident} that supports expiration
 * and user engagement (confirms/denies).
 *
 * An Incident starts with a fixed lifespan of 30 minutes (see {@link Expirable#TTL}),
 * which can be extended by user confirmations (but never beyond the max 30 minutes).
 * It can also be deleted if it either expires naturally or receives 3 consecutive denies.
 */
@Getter
public class Incident implements Expirable, Actored, Locatable, HasMedia, Reactable {

  private final ActorId actorId;
  private final LocationId locationId;
  private final SentimentEngagement sentimentEngagement;
  private final Set<Media> media;
  private final String title;
  private final String description;
  private EngagementStats engagementStats;

  /**
   * Holds the current expiration timestamp (mutable).
   * Initialized to the default (createdAt + TTL) using the interface default.
   */
  private Instant expiresAt;

  /**
   * Extension applied when a confirmation is added (capped at 30 minutes max).
   */
  private static final Duration EXTENSION = Duration.ofMinutes(5);

  /**
   * Constructs a new {@code Incident} instance with initial values.
   *
   * @param actorId     the actor who created the incident
   * @param locationId  the location where the incident is associated
   * @param media       the media associated with the incident
   * @param title       the title of the incident
   * @param description the description of the incident
   */
  @Builder(toBuilder = true)
  public Incident(
      ActorId actorId,
      LocationId locationId,
      String title,
      String description,
      Set<Media> media,
      SentimentEngagement sentimentEngagement,
      EngagementStats engagementStats) {

    this.actorId = actorId;
    this.locationId = locationId;
    this.title = title;
    this.description = description;
    this.media = Collections.unmodifiableSet(media);
    this.sentimentEngagement = sentimentEngagement;
    this.engagementStats = new EngagementStats(0, 0, 0);
    this.expiresAt = Instant.now().plus(TTL);
  }

  /**
   * Checks whether the incident should be deleted.
   * An incident is deleted if it is expired or has at least 3 consecutive denies.
   *
   * @return {@code true} if the incident should be removed.
   */
  public boolean isDeleted() {
    return engagementStats.consecutiveDenies() >= 3 || isExpired();
  }

  /**
   * Confirms the incident.
   *
   * - Increments the confirmation counter.
   * - Resets the consecutive denies counter.
   * - Extends expiration time by 5 minutes, but never beyond 30 minutes
   *   from creation.
   */
  public synchronized void confirmIncident() {
    if (Instant.now().isAfter(expiresAt)) return;

    Instant maxExpiry = createdAt().plus(TTL); // now +30 minutes
    Instant newExpiry = getExpiresAt().plus(EXTENSION); // +5 minutes

    if (newExpiry.isAfter(maxExpiry)) newExpiry = maxExpiry;

    engagementStats = engagementStats.addConfirm();
    this.overrideExpiresAt(newExpiry);
  }

  /**
   * Denies the incident.
   *
   * - Increments the denial counter.
   * - Increments the consecutive denial counter.
   */
  public synchronized void denyIncident() {
    engagementStats = engagementStats.addDeny();
  }

  /**
   * Returns the current expiration timestamp of the incident.
   * This may differ from the default (createdAt + 30m) if confirmations
   * have extended the incident's lifespan.
   *
   * @return the expiration time
   */
  @Override
  public Instant getExpiresAt() {
    return expiresAt;
  }

  /**
   * Updates the expiration timestamp of the incident.
   *
   * @param newExpiry the new expiration time
   */
  private void overrideExpiresAt(Instant newExpiry) {
    this.expiresAt = newExpiry;
  }

}
