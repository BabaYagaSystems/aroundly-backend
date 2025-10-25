package com.backend.services;

import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.port.inbound.commands.CoordinatesCommand;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.port.inbound.commands.UploadMediaCommand;
import com.backend.port.outbound.repo.IncidentRepository;
import com.backend.port.outbound.storage.ObjectStoragePort;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    private Incident incident;
    private Set<Media> media;
    private Location location;
    @Mock private IncidentRepository incidentRepository;
    @Mock private LocationService locationService;
    @Mock private ObjectStoragePort objectStoragePort;
    @InjectMocks private IncidentService incidentService;

    private static final long INCIDENT_ID = 1L;
    
    @BeforeEach
    void setup() {
      media = Set.of(new Media(3L, "file", "type"));
      
      location = new Location(
        new LocationId(1L),
        47.0101, 28.8576,
        "str. new 1");
      
      incident = Incident.builder()
        .locationId(new LocationId(1L))
        .title("title")
        .description("description")
        .media(media)
        .build();
    }

    @Test
    void testFindAllInGivenRange() {
        double lat = 47.0101;
        double lon = 28.8576;
        double radius = 1500.0;

        RadiusCommand radiusCommand = new RadiusCommand(lat, lon, radius);

        when(incidentRepository.findAllInGivenRange(lat, lon, radius))
            .thenReturn(List.of(incident));
        List<Incident> result = incidentService.findAllInGivenRange(radiusCommand);

        assertEquals(result, List.of(incident));
    }

    @Test
    void testFindByActorId() {
        final ActorId actorId = new ActorId("abc-123");
        when(incidentRepository.findByUserId(actorId.id())).thenReturn(List.of(incident));

        List<Happening> result = incidentService.findByActorId(actorId.id());

        assertEquals(result, List.of(incident));
    }

    @Test
    void testFindById() {
        when(incidentRepository.findById(INCIDENT_ID)).thenReturn(Optional.ofNullable(
            incident));
        Incident result = incidentService.findById(INCIDENT_ID);

        assertEquals(incident, result);
    }

    @Test
    void testCreateIncident() throws Exception {
        final CreateIncidentCommand command = mock(CreateIncidentCommand.class);
        when(command.title()).thenReturn("title");
        when(command.description()).thenReturn("description");
        when(command.lat()).thenReturn(location.latitude());
        when(command.lon()).thenReturn(location.longitude());

        CoordinatesCommand coordinatesCommand = new CoordinatesCommand(command.lat(), command.lon());
        when(locationService.findByCoordinates(coordinatesCommand))
            .thenReturn(location);
        when(objectStoragePort.uploadAll(Mockito.<Set<UploadMediaCommand>>any()))
            .thenReturn(media);
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Incident result = incidentService.create(command);

        assertEquals(incident.getTitle(), result.getTitle());
        assertEquals(incident.getDescription(), result.getDescription());
        assertEquals(incident.getMedia(), result.getMedia());
        verify(locationService).findByCoordinates(coordinatesCommand);
    }

    @Test
    void testDeleteIncident() {
        when(incidentRepository.existsById(INCIDENT_ID)).thenReturn(true);
        incidentService.deleteById(INCIDENT_ID);
        verify(incidentRepository, times(1)).deleteById(INCIDENT_ID);
    }
    
}
