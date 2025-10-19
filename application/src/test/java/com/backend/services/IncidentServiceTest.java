package com.backend.services;

import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Happening;
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

    private static final long INCIDENT_ID = 1L;

    @Mock private IncidentRepository incidentRepository;
    @Mock private LocationService locationService;
    @Mock private ObjectStoragePort objectStoragePort;
    @InjectMocks private IncidentService incidentService;

    @Test
    void testFindAllInGivenRange() {
        double lat = 47.0101;
        double lon = 28.8576;
        double radius = 1500.0;

        RadiusCommand radiusCommand = new RadiusCommand(lat, lon, radius);

        when(incidentRepository.findAllInGivenRange(lat, lon, radius))
            .thenReturn(List.of(createIncident()));
        List<Incident> result = incidentService.findAllInGivenRange(radiusCommand);

        assertEquals(result, List.of(createIncident()));
    }

    @Test
    void testFindByActorId() {
        final ActorId actorId = new ActorId("abc-123");
        when(incidentRepository.findByUserId(actorId.id())).thenReturn(List.of(createIncident()));

        List<Happening> result = incidentService.findByActorId(actorId.id());

        assertEquals(result, List.of(createIncident()));
    }

    @Test
    void testFindById() {
        when(incidentRepository.findById(INCIDENT_ID)).thenReturn(Optional.ofNullable(
            createIncident()));
        Happening result = incidentService.findById(INCIDENT_ID);

        assertEquals(createIncident(), result);
    }

    @Test
    void testCreateIncident() throws Exception {
        final CreateIncidentCommand command = mock(CreateIncidentCommand.class);
        when(command.title()).thenReturn("title");
        when(command.description()).thenReturn("description");
        when(command.lat()).thenReturn(createLocation().latitude());
        when(command.lon()).thenReturn(createLocation().longitude());

        final Location resolvedLocation = createLocation();
        final Incident expectedIncident = createIncident();

        CoordinatesCommand coordinatesCommand = new CoordinatesCommand(command.lat(), command.lon());
        when(locationService.findByCoordinates(coordinatesCommand))
            .thenReturn(resolvedLocation);
        when(objectStoragePort.uploadAll(Mockito.<Set<UploadMediaCommand>>any()))
            .thenReturn(createMedia());
        when(incidentRepository.save(createIncident())).thenReturn(createIncident());
        Incident result = incidentService.create(command);

        assertEquals(expectedIncident, result);
        verify(locationService).findByCoordinates(coordinatesCommand);
        verify(incidentRepository).save(expectedIncident);
    }

    @Test
    void testUpdateIncident() {
        when(incidentRepository.findById(INCIDENT_ID))
            .thenReturn(Optional.ofNullable(createIncident()));
        Incident expectedUpdatedOldIncident = createIncident().toBuilder()
                .title("new title for incident")
                .build();

        when(incidentRepository.save(expectedUpdatedOldIncident))
            .thenReturn(expectedUpdatedOldIncident);

        final CreateIncidentCommand updatedCommand = mock(CreateIncidentCommand.class);
        when(updatedCommand.title()).thenReturn("new title");
        when(updatedCommand.description()).thenReturn("new description");

        Incident updatedOldIncident = incidentService.update(INCIDENT_ID, updatedCommand);

        assertEquals(expectedUpdatedOldIncident, updatedOldIncident);
    }

    @Test
    void testDeleteIncident() {
        when(incidentRepository.existsById(INCIDENT_ID)).thenReturn(true);
        incidentService.deleteById(INCIDENT_ID);
        verify(incidentRepository, times(1)).deleteById(INCIDENT_ID);
    }

    private Set<Media> createMedia()  {
        return Set.of(new Media(3L, "file", "type"));
    }

    private Incident createIncident() {
        return Incident.builder()
            .actorId(new ActorId("id"))
            .locationId(new LocationId(1L))
            .title("title")
            .description("description")
            .media(createMedia())
            .build();
    }

    private Location createLocation() {
        return new Location(
            new LocationId(1L),
            47.0101, 28.8576,
            "str. new 1");
    }
}
