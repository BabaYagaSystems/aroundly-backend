package com.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backend.domain.actor.UserId;
import com.backend.domain.happening.Incident;
import com.backend.domain.happening.IncidentId;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.reactions.EngagementStats;
import com.backend.domain.reactions.SentimentEngagement;
import com.backend.domain.reactions.IncidentEngagementType;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.port.inbound.commands.UploadMediaCommand;
import com.backend.port.outbound.repo.IncidentEngagementRepository;
import com.backend.port.outbound.repo.IncidentRepository;
import com.backend.port.outbound.repo.LocationRepository;
import com.backend.port.outbound.storage.ObjectStoragePort;
import com.backend.services.exceptions.IncidentNotExpiredException;
import com.backend.services.exceptions.InvalidCoordinatesException;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncidentServiceTest {

  private IncidentService incidentService;
  private InMemoryIncidentRepository incidentRepository;
  private InMemoryIncidentEngagementRepository engagementRepository;
  private FakeObjectStoragePort objectStoragePort;
  private InMemoryLocationRepository locationRepository;
  private TestLocationService locationService;

  @BeforeEach
  void setUp() {
    incidentRepository = new InMemoryIncidentRepository();
    engagementRepository = new InMemoryIncidentEngagementRepository();
    objectStoragePort = new FakeObjectStoragePort();
    locationRepository = new InMemoryLocationRepository();
    locationService = new TestLocationService(locationRepository);
    incidentService =
        new IncidentService(
            incidentRepository, engagementRepository, objectStoragePort, locationService);

    locationRepository.save(new Location(new LocationId(1L), 9.0, 42.0, "Central Park"));
  }

  @Test
  void createPersistsIncidentWithUploadedMedia() throws Exception {
    CreateIncidentCommand command =
        CreateIncidentCommand.builder()
            .userId(new UserId("firebase-1"))
            .title("Road obstruction")
            .description("Tree fell on the road")
            .media(Set.of(upload("blocked.png")))
            .lat(42.0)
            .lon(9.0)
            .build();

    Incident created = incidentService.create(command);

    assertThat(created.getId()).isNotNull();
    assertThat(created.getLocationId().value()).isEqualTo(1L);
    assertThat(created.getMedia())
        .extracting(Media::filename)
        .containsExactly("blocked.png");
    assertThat(incidentRepository.findById(created.getId().value())).contains(created);
  }

  @Test
  void confirmAddsEngagementAndUpdatesStats() throws Exception {
    Incident incident = sampleIncident(1L);
    incidentRepository.save(incident);
    UserId userId = new UserId("user-123");

    Incident updated = incidentService.confirm(1L, userId);

    assertThat(updated.getEngagementStats().confirms()).isEqualTo(1);
    assertThat(engagementRepository.findUserEngagement(1L, userId))
        .contains(IncidentEngagementType.CONFIRM);
  }

  @Test
  void denyAddsEngagementAndUpdatesStats() throws Exception {
    Incident incident = sampleIncident(2L);
    incidentRepository.save(incident);
    UserId userId = new UserId("user-456");

    Incident updated = incidentService.deny(2L, userId);

    assertThat(updated.getEngagementStats().denies()).isEqualTo(1);
    assertThat(updated.getEngagementStats().consecutiveDenies()).isEqualTo(1);
    assertThat(engagementRepository.findUserEngagement(2L, userId))
        .contains(IncidentEngagementType.DENY);
  }

  @Test
  void deleteIfExpiredRemovesIncidentWhenMarkedDeleted() throws Exception {
    Incident expired =
        Incident.builder()
            .id(new IncidentId(3L))
            .userId(new UserId("owner"))
            .locationId(new LocationId(1L))
            .title("Flooding")
            .description("River overflow")
            .media(Set.of(new Media(1, "flood.png", "image/png")))
            .sentimentEngagement(SentimentEngagement.builder().likes(0).dislikes(0).build())
            .engagementStats(new EngagementStats(0, 3, 3))
            .build();
    incidentRepository.save(expired);

    incidentService.deleteIfExpired(3L);

    assertThat(incidentRepository.existsById(3L)).isFalse();
  }

  @Test
  void findAllInGivenRangeRejectsLargeRadius() {
    RadiusCommand command = new RadiusCommand(0.0, 0.0, 60000);

    assertThrows(InvalidCoordinatesException.class, () -> incidentService.findAllInGivenRange(command));
  }

  private static UploadMediaCommand upload(String filename) {
    return new UploadMediaCommand(new ByteArrayInputStream(new byte[] {1}), filename, 1L, "image/png");
  }

  private static Incident sampleIncident(long id) {
    return Incident.builder()
        .id(new IncidentId(id))
        .userId(new UserId("owner"))
        .locationId(new LocationId(1L))
        .title("Pothole")
        .description("Large pothole in the street")
        .media(Set.of(new Media(1, "pothole.png", "image/png")))
        .sentimentEngagement(SentimentEngagement.builder().likes(0).dislikes(0).build())
        .engagementStats(new EngagementStats(0, 0, 0))
        .build();
  }

  private static class InMemoryIncidentRepository implements IncidentRepository {
    private final Map<Long, Incident> storage = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Optional<Incident> findById(long incidentId) {
      return Optional.ofNullable(storage.get(incidentId));
    }

    @Override
    public boolean existsById(long incidentId) {
      return storage.containsKey(incidentId);
    }

    @Override
    public List<Incident> findByUserId(String userId) {
      return storage.values().stream()
          .filter(incident -> incident.getUserId().value().equals(userId))
          .collect(Collectors.toList());
    }

    @Override
    public void deleteById(long incidentId) {
      storage.remove(incidentId);
    }

    @Override
    public Incident save(Incident incident) {
      Incident stored = incident;
      if (incident.getId() == null) {
        stored = incident.toBuilder().id(new IncidentId(sequence.getAndIncrement())).build();
      }
      storage.put(stored.getId().value(), stored);
      return stored;
    }

    @Override
    public List<Incident> findAllInGivenRange(double lat, double lon, double radiusMeters) {
      return new ArrayList<>(storage.values());
    }
  }

  private static class InMemoryIncidentEngagementRepository implements IncidentEngagementRepository {
    private final Map<Long, Map<String, IncidentEngagementType>> engagements = new HashMap<>();

    @Override
    public Optional<IncidentEngagementType> findUserEngagement(long incidentId, UserId userId) {
      return Optional.ofNullable(
          engagements.getOrDefault(incidentId, Map.of()).get(userId.value()));
    }

    @Override
    public void saveEngagement(long incidentId, UserId userId, IncidentEngagementType type) {
      engagements
          .computeIfAbsent(incidentId, id -> new HashMap<>())
          .put(userId.value(), type);
    }
  }

  private static class FakeObjectStoragePort implements ObjectStoragePort {

    @Override
    public Set<Media> uploadAll(Set<UploadMediaCommand> uploads) {
      return uploads.stream()
          .map(upload -> new Media(upload.size(), upload.filename(), upload.contentType()))
          .collect(Collectors.toSet());
    }

    @Override
    public void deleteAllByKeys(Set<String> keys) {
      // no-op for tests
    }

    @Override
    public String presignGet(String key, Duration ttl) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String presignPut(String key, Duration ttl) {
      throw new UnsupportedOperationException();
    }
  }

  private static class InMemoryLocationRepository implements LocationRepository {
    private final Map<Long, Location> storage = new HashMap<>();
    private final Map<String, Location> coordinateIndex = new HashMap<>();
    private long sequence = 1L;

    @Override
    public Location save(Location location) {
      LocationId id = location.id() != null ? location.id() : new LocationId(sequence++);
      Location stored =
          new Location(id, location.longitude(), location.latitude(), location.address());
      storage.put(id.value(), stored);
      coordinateIndex.put(key(stored.latitude(), stored.longitude()), stored);
      return stored;
    }

    @Override
    public Location findById(long id) {
      return storage.get(id);
    }

    @Override
    public Optional<Location> findByCoordinate(double latitude, double longitude) {
      return Optional.ofNullable(coordinateIndex.get(key(latitude, longitude)));
    }

    @Override
    public void deleteById(long id) {
      Location removed = storage.remove(id);
      if (removed != null) {
        coordinateIndex.remove(key(removed.latitude(), removed.longitude()));
      }
    }

    private String key(double latitude, double longitude) {
      return latitude + ":" + longitude;
    }
  }

  private static class TestLocationService extends LocationService {
    private final LocationRepository repository;

    TestLocationService(LocationRepository repository) {
      super(repository, "test-token");
      this.repository = repository;
    }

    @Override
    public Location findByCoordinates(com.backend.port.inbound.commands.CoordinatesCommand command) {
      return repository.findByCoordinate(command.lat(), command.lon())
          .orElseThrow(() -> new IllegalStateException("Missing coordinates in test repository"));
    }
  }
}
