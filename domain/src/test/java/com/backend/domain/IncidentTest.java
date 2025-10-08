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
        incident = createIncident();
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
        Instant expiry = incident.expiresAt();
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

        incident.confirmIncident();
        assertEquals(15, Duration.between(created, getExpiresAt(incident)).toMinutes());

        incident.confirmIncident();
        assertEquals(20, Duration.between(created, getExpiresAt(incident)).toMinutes());

        incident.confirmIncident();
        assertEquals(25, Duration.between(created, getExpiresAt(incident)).toMinutes());

        incident.confirmIncident();
        assertEquals(30, Duration.between(created, getExpiresAt(incident)).toMinutes());

        incident.confirmIncident();
        assertEquals(30, Duration.between(created, getExpiresAt(incident)).toMinutes());
    }

    @Test
    public void testDenyIncident() {
        setExpiresAt(incident, Instant.now().plus(Duration.ofHours(30)));
        assertFalse(incident.isDeleted());
        incident.denyIncident();
        assertFalse(incident.isDeleted());
        incident.denyIncident();
        assertFalse(incident.isDeleted());
        incident.denyIncident();
        assertTrue(incident.isDeleted(), "Three consecutive denies should delete the incident");
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

    private Incident createIncident() {
        return new Incident(
                1L,
                new ActorId("id"),
                new LocationId(1L),
                "title",
                "description",
                Set.of(new Media(3L, "file", "type"))
        );
    }
}
