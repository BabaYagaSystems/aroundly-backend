package com.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.domain.actor.UserId;
import com.backend.domain.happening.Incident;
import com.backend.domain.happening.IncidentId;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.reactions.EngagementStats;
import com.backend.domain.reactions.IncidentEngagementType;
import com.backend.domain.reactions.SentimentEngagement;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.port.inbound.commands.UploadMediaCommand;
import com.backend.port.outbound.repo.IncidentEngagementRepository;
import com.backend.port.outbound.repo.IncidentRepository;
import com.backend.port.outbound.storage.ObjectStoragePort;
import com.backend.services.exceptions.InvalidCoordinatesException;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

  @Mock
  private IncidentRepository incidentRepository;

  @Mock
  private IncidentEngagementRepository incidentEngagementRepository;

  @Mock
  private ObjectStoragePort objectStoragePort;

  @Mock
  private LocationService locationService;

  @InjectMocks
  private IncidentService incidentService;

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
    Location location = new Location(new LocationId(1L), 9.0, 42.0, "Central Park");
    Set<Media> storedMedia = Set.of(new Media(1, "blocked.png", "image/png"));

    when(locationService.findByCoordinates(any())).thenReturn(location);
    when(objectStoragePort.uploadAll(command.media())).thenReturn(storedMedia);
    when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
      Incident payload = invocation.getArgument(0);
      return payload.toBuilder().id(new IncidentId(10L)).build();
    });

    Incident created = incidentService.create(command);

    assertThat(created.getId().value()).isEqualTo(10L);
    assertThat(created.getLocationId().value()).isEqualTo(1L);
    assertThat(created.getMedia()).isEqualTo(storedMedia);
    verify(locationService).findByCoordinates(any());
    verify(objectStoragePort).uploadAll(command.media());
    verify(incidentRepository).save(any(Incident.class));
  }

  @Test
  void confirmAddsEngagementAndUpdatesStats() throws Exception {
    Incident incident = sampleIncident(1L);
    UserId userId = new UserId("user-123");

    when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
    when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(incidentEngagementRepository.findUserEngagement(1L, userId)).thenReturn(Optional.empty());

    Incident updated = incidentService.confirm(1L, userId);

    assertThat(updated.getEngagementStats().confirms()).isEqualTo(1);
    verify(incidentEngagementRepository).saveEngagement(1L, userId, IncidentEngagementType.CONFIRM);
    verify(incidentRepository).save(updated);
  }

  @Test
  void denyAddsEngagementAndUpdatesStats() throws Exception {
    Incident incident = sampleIncident(2L);
    UserId userId = new UserId("user-456");

    when(incidentRepository.findById(2L)).thenReturn(Optional.of(incident));
    when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(incidentEngagementRepository.findUserEngagement(2L, userId)).thenReturn(Optional.empty());

    Incident updated = incidentService.deny(2L, userId);

    assertThat(updated.getEngagementStats().denies()).isEqualTo(1);
    assertThat(updated.getEngagementStats().consecutiveDenies()).isEqualTo(1);
    verify(incidentEngagementRepository).saveEngagement(2L, userId, IncidentEngagementType.DENY);
    verify(incidentRepository).save(updated);
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

    when(incidentRepository.findById(3L)).thenReturn(Optional.of(expired));
    doNothing().when(incidentRepository).deleteById(3L);

    incidentService.deleteIfExpired(3L);

    verify(incidentRepository).deleteById(3L);
  }

  @Test
  void findAllInGivenRangeRejectsLargeRadius() {
    RadiusCommand command = new RadiusCommand(0.0, 0.0, 60000);

    assertThrows(InvalidCoordinatesException.class, () -> incidentService.findAllInGivenRange(command));
    verify(incidentRepository, never()).findAllInGivenRange(anyDouble(), anyDouble(), anyDouble());
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
}
