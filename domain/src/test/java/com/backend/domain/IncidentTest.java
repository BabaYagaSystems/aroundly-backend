package com.backend.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IncidentTest {

  private Incident incident;

  @BeforeEach
  void setup() throws URISyntaxException {
    incident = Incident.builder()
            .actorId(new ActorId("abc"))
            .locationId(new LocationId(1L))
            .title("title")
            .description("description")
            .media(Set.of(new Media(3L, "file", "type")))
            .build();
  }

  @Test
  public void testIncidentCreationTime() {
    int actualCreationTime = LocalDateTime.ofInstant(
            incident.createdAt(), ZoneId.systemDefault()).getMinute();
    int expectedCreationTime = LocalDateTime.now().getMinute();

    assert expectedCreationTime == actualCreationTime;
  }

  @Test
  public void testIncidentIsNotExpired() {
    assertFalse(incident.isExpired());
  }

  @Test
  public void testIncidentExpired() {
    Instant expiry = incident.getExpiresAt();
    Instant fakeNow = expiry.plus(Duration.ofHours(1));
    Clock fakeClock = Clock.fixed(fakeNow, ZoneOffset.UTC);

    assertTrue(incident.isExpired(fakeClock));
  }

  @Test
  public void testIncidentIsNotReadyToDelete() {
    assertFalse(incident.isDeleted());
  }

  @Test
  public void testConfirmIncident() {
    Instant created = incident.createdAt();
    setExpiresAt(incident, created.plus(Duration.ofMinutes(10)));

    // Confirm 1-4: No change (threshold not reached)
    incident.confirmIncident();
    assertEquals(10, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "First confirm should not change expiry");

    incident.confirmIncident();
    assertEquals(10, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Second confirm should not change expiry");

    incident.confirmIncident();
    assertEquals(10, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Third confirm should not change expiry");

    incident.confirmIncident();
    assertEquals(10, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Fourth confirm should not change expiry");

    // Confirm 5: Should add 2 minutes (10 + 2 = 12)
    incident.confirmIncident();
    assertEquals(12, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Fifth confirm should add 2 minutes");

    // Confirm 6-9: No change
    incident.confirmIncident();
    incident.confirmIncident();
    incident.confirmIncident();
    incident.confirmIncident();
    assertEquals(12, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Confirms 6-9 should not change expiry");

    // Confirm 10: Should add 2 more minutes (12 + 2 = 14)
    incident.confirmIncident();
    assertEquals(14, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Tenth confirm should add 2 minutes");
  }

  @Test
  public void testConfirmIncidentRespects30MinutesCap() {
    Instant created = incident.createdAt();
    // Set expiry to 28 minutes from creation
    setExpiresAt(incident, created.plus(Duration.ofMinutes(28)));

    // Manually set confirms to 4 so next confirm will trigger extension
    setEngagementStats(incident, 4, 0, 0);

    // This 5th confirm should only extend to 30 minutes max, not 30 (28+2)
    incident.confirmIncident();
    assertEquals(30, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Extension should be capped at 30 minutes from creation");
  }

  @Test
  public void testDenyIncident() {
    setExpiresAt(incident, Instant.now().plus(Duration.ofHours(30)));
    assertFalse(incident.isDeleted());

    incident.denyIncident();
    assertFalse(incident.isDeleted(), "One deny should not delete incident");

    incident.denyIncident();
    assertFalse(incident.isDeleted(), "Two denies should not delete incident");

    incident.denyIncident();
    assertTrue(incident.isDeleted(), "Three consecutive denies should delete the incident");
  }

  @Test
  public void testDenyIncidentReducesExpirationTime() {
    Instant created = incident.createdAt();
    // Set expiry to 20 minutes from creation
    setExpiresAt(incident, created.plus(Duration.ofMinutes(20)));

    // First two denies should not change expiration
    incident.denyIncident();
    assertEquals(20, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "First deny should not change expiry");

    incident.denyIncident();
    assertEquals(20, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Second deny should not change expiry");

    // Third consecutive deny should reduce by 5 minutes (20 - 5 = 15)
    incident.denyIncident();
    assertEquals(15, Duration.between(created, getExpiresAt(incident)).toMinutes(),
            "Third consecutive deny should reduce expiry by 5 minutes");
  }

  @Test
  public void testDenyIncidentCannotSetExpiryBeforeNow() {
    Instant now = Instant.now();
    // Set expiry to only 2 minutes in the future
    setExpiresAt(incident, now.plus(Duration.ofMinutes(2)));

    // Manually set consecutive denies to 2
    setEngagementStats(incident, 0, 2, 2);

    // Third deny would normally subtract 5 minutes, but should be capped at 'now'
    incident.denyIncident();

    Instant actualExpiry = getExpiresAt(incident);
    assertTrue(!actualExpiry.isBefore(now),
            "Expiry should not be set before current time");
  }

  @Test
  public void testConfirmResetsConsecutiveDenies() {
    setExpiresAt(incident, Instant.now().plus(Duration.ofHours(1)));

    incident.denyIncident();
    incident.denyIncident();
    assertEquals(2, incident.getEngagementStats().consecutiveDenies());

    incident.confirmIncident();
    assertEquals(0, incident.getEngagementStats().consecutiveDenies(),
            "Confirm should reset consecutive denies to 0");
  }

  private static Instant getExpiresAt(Incident i) {
    try {
      Field f = Incident.class.getDeclaredField("expiresAt");
      f.setAccessible(true);
      return (Instant) f.get(i);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void setExpiresAt(Incident i, Instant value) {
    try {
      Field f = Incident.class.getDeclaredField("expiresAt");
      f.setAccessible(true);
      f.set(i, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void setEngagementStats(Incident i, int confirms, int denies, int consecutiveDenies) {
    try {
      Field f = Incident.class.getDeclaredField("engagementStats");
      f.setAccessible(true);
      f.set(i, new com.backend.domain.reactions.EngagementStats(confirms, denies, consecutiveDenies));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}