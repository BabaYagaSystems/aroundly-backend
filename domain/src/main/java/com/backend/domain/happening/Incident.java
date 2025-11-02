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

  private final long id;
  private final long actorId;
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
   * Extension applied every 5 confirmations: +2 minutes (capped at 30 minutes max from creation).
   */
  private static final Duration CONFIRM_EXTENSION = Duration.ofMinutes(2);
  private static final int CONFIRMS_THRESHOLD = 5;

  /**
   * Reduction applied every 3 consecutive denies: -5 minutes (but never before current time).
   */
  private static final Duration DENY_REDUCTION = Duration.ofMinutes(5);
  private static final int DENIES_THRESHOLD = 3;

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
      long id,
      long actorId,
      LocationId locationId,
      String title,
      String description,
      Set<Media> media,
      SentimentEngagement sentimentEngagement,
      EngagementStats engagementStats,
      Instant expiresAt) {

    this.id = id;
    this.actorId = actorId;
    this.locationId = locationId;
    this.title = title;
    this.description = description;
    this.media = Collections.unmodifiableSet(media);
    this.sentimentEngagement = sentimentEngagement != null
        ? sentimentEngagement
        : SentimentEngagement.builder().dislikes(0).likes(0).build();
    this.engagementStats = engagementStats != null
        ? engagementStats
        : new EngagementStats(0, 0, 0);
    this.expiresAt = expiresAt != null ? expiresAt : Instant.now().plus(TTL);;
  }

  /**
   * Checks whether the incident should be deleted.
   * An incident is deleted if it is expired or has at least 3 consecutive denies.
   *
   * @return {@code true} if the incident should be removed.
   */
  public boolean isDeleted() {
    return engagementStats.consecutiveDenies() >= DENIES_THRESHOLD || isExpired();
  }

  /**
   * Confirms the incident.
   *
   * - Increments the confirmation counter.
   * - Resets the consecutive denies counter.
   * - Every 5 confirms: extends expiration time by 2 minutes (capped at 30 minutes from creation).
   */
  public synchronized void confirmIncident() {
    if (Instant.now().isAfter(expiresAt)) return;

    int oldConfirms = engagementStats.confirms();
    engagementStats = engagementStats.addConfirm();
    int newConfirms = engagementStats.confirms();

    // Check if we crossed a threshold of 5 confirms
    if (newConfirms % CONFIRMS_THRESHOLD == 0 && newConfirms > oldConfirms) {
      Instant maxExpiry = createdAt().plus(TTL); // creation time + 30 minutes
      Instant newExpiry = getExpiresAt().plus(CONFIRM_EXTENSION); // current expiry + 2 minutes

      // Cap at 30 minutes from creation
      if (newExpiry.isAfter(maxExpiry)) {
        newExpiry = maxExpiry;
      }

      this.overrideExpiresAt(newExpiry);
    }
  }

  /**
   * Denies the incident.
   *
   * - Increments the denial counter.
   * - Increments the consecutive denial counter.
   * - Every 3 consecutive denies: reduces expiration time by 5 minutes (but never before current time).
   */
  public synchronized void denyIncident() {
    int oldConsecutiveDenies = engagementStats.consecutiveDenies();
    engagementStats = engagementStats.addDeny();
    int newConsecutiveDenies = engagementStats.consecutiveDenies();

    // Check if we reached exactly 3 consecutive denies
    if (newConsecutiveDenies == DENIES_THRESHOLD && newConsecutiveDenies > oldConsecutiveDenies) {
      Instant now = Instant.now();
      Instant newExpiry = getExpiresAt().minus(DENY_REDUCTION); // current expiry - 5 minutes

      // Never set expiry before current time
      if (newExpiry.isBefore(now)) {
        newExpiry = now;
      }

      this.overrideExpiresAt(newExpiry);
    }
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
