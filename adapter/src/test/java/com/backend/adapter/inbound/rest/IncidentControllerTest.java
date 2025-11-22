package com.backend.adapter.inbound.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.request.IncidentRequestDto;
import com.backend.adapter.inbound.dto.request.RadiusRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.IncidentResponseMapper;
import com.backend.adapter.inbound.websocket.IncidentBroadcast;
import com.backend.domain.actor.Role;
import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import com.backend.domain.happening.Incident;
import com.backend.domain.happening.IncidentId;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.reactions.EngagementStats;
import com.backend.domain.reactions.SentimentEngagement;
import com.backend.port.inbound.IncidentUseCase;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.services.UserService;
import com.backend.services.exceptions.ActorNotFoundException;
import com.backend.services.exceptions.DuplicateIncidentException;
import com.backend.services.exceptions.IncidentAlreadyConfirmedException;
import com.backend.services.exceptions.IncidentNotExpiredException;
import com.backend.services.exceptions.IncidentNotFoundException;
import com.backend.services.exceptions.InvalidCoordinatesException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class IncidentControllerTest {

  @Mock private IncidentUseCase incidentUseCase;
  @Mock private IncidentResponseMapper incidentResponseMapper;
  @Mock private UserService userService;
  @Mock private IncidentBroadcast incidentBroadcast;

  @InjectMocks private IncidentController controller;

  private Incident incident;
  private IncidentDetailedResponseDto detailedResponse;
  private IncidentPreviewResponseDto previewResponse;

  @BeforeEach
  void setUp() {
    incident = sampleIncident();
    detailedResponse = sampleDetailedResponse();
    previewResponse = samplePreviewResponse();
  }

  @Test
  void createReturnsCreatedResponse() {
    IncidentRequestDto request = incidentRequest();
    CreateIncidentCommand command = new CreateIncidentCommand(
        new UserId("uid-1"), "title", "description", Set.of(), 10.0, 20.0);

    when(incidentResponseMapper.toCreateIncidentCommand(request)).thenReturn(command);
    when(incidentUseCase.create(command)).thenReturn(incident);
    when(incidentResponseMapper.toIncidentDetailedResponseDto(incident)).thenReturn(detailedResponse);

    ResponseEntity<IncidentDetailedResponseDto> response = controller.create(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isSameAs(detailedResponse);
    verify(incidentUseCase).create(command);
  }

  @Test
  void createReturnsConflictOnDuplicateIncident() {
    IncidentRequestDto request = incidentRequest();
    CreateIncidentCommand command = new CreateIncidentCommand(
        new UserId("uid-1"), "title", "description", Set.of(), 10.0, 20.0);

    when(incidentResponseMapper.toCreateIncidentCommand(request)).thenReturn(command);
    when(incidentUseCase.create(command)).thenThrow(new DuplicateIncidentException("duplicate"));

    ResponseEntity<IncidentDetailedResponseDto> response = controller.create(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void createReturnsBadRequestOnValidationFailure() {
    IncidentRequestDto request = incidentRequest();
    CreateIncidentCommand command = new CreateIncidentCommand(
        new UserId("uid-1"), "title", "description", Set.of(), 10.0, 20.0);

    when(incidentResponseMapper.toCreateIncidentCommand(request)).thenReturn(command);
    when(incidentUseCase.create(command)).thenThrow(new jakarta.validation.ValidationException("invalid"));

    ResponseEntity<IncidentDetailedResponseDto> response = controller.create(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateReturnsUpdatedResponse() {
    IncidentRequestDto request = incidentRequest();
    CreateIncidentCommand command = new CreateIncidentCommand(
        new UserId("uid-1"), "title", "description", Set.of(), 10.0, 20.0);

    when(incidentResponseMapper.toCreateIncidentCommand(request)).thenReturn(command);
    when(incidentUseCase.update(1L, command)).thenReturn(incident);
    when(incidentResponseMapper.toIncidentDetailedResponseDto(incident)).thenReturn(detailedResponse);

    ResponseEntity<IncidentDetailedResponseDto> response = controller.update(1L, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(detailedResponse);
  }

  @Test
  void updateReturnsNotFoundWhenIncidentMissing() {
    IncidentRequestDto request = incidentRequest();
    CreateIncidentCommand command = new CreateIncidentCommand(
        new UserId("uid-1"), "title", "description", Set.of(), 10.0, 20.0);

    when(incidentResponseMapper.toCreateIncidentCommand(request)).thenReturn(command);
    when(incidentUseCase.update(1L, command)).thenThrow(new IncidentNotFoundException("missing"));

    ResponseEntity<IncidentDetailedResponseDto> response = controller.update(1L, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getIncidentInPreviewReturnsDto() {
    when(incidentUseCase.findById(5L)).thenReturn(incident);
    when(incidentResponseMapper.toIncidentPreviewResponseDto(incident)).thenReturn(previewResponse);

    ResponseEntity<IncidentPreviewResponseDto> response = controller.getIncidentInPreview(5L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(previewResponse);
  }

  @Test
  void getIncidentInPreviewReturnsBadRequestWhenNotFound() {
    when(incidentUseCase.findById(5L)).thenThrow(new IncidentNotFoundException("missing"));

    ResponseEntity<IncidentPreviewResponseDto> response = controller.getIncidentInPreview(5L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void findActorIncidentsReturnsEmptyWhenActorMissing() {
    when(incidentUseCase.findByUserId("actor")).thenThrow(new ActorNotFoundException("missing"));

    ResponseEntity<List<IncidentPreviewResponseDto>> response =
        controller.findActorIncidentsInPreview("actor");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  void findActorIncidentsReturnsMappedDtos() {
    when(incidentUseCase.findByUserId("actor")).thenReturn(List.of(incident));
    when(incidentResponseMapper.toIncidentPreviewResponseDto(incident)).thenReturn(previewResponse);

    ResponseEntity<List<IncidentPreviewResponseDto>> response =
        controller.findActorIncidentsInPreview("actor");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactly(previewResponse);
  }

  @Test
  void findNearbyIncidentsReturnsMappedDtos() {
    RadiusRequestDto request = new RadiusRequestDto(10.0, 20.0, 500);
    when(incidentUseCase.findAllInGivenRange(any(RadiusCommand.class))).thenReturn(List.of(incident));
    when(incidentResponseMapper.toIncidentPreviewResponseDto(incident)).thenReturn(previewResponse);

    ResponseEntity<List<IncidentPreviewResponseDto>> response =
        controller.findNearbyIncidents(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactly(previewResponse);
    verify(incidentUseCase).findAllInGivenRange(any(RadiusCommand.class));
  }

  @Test
  void findNearbyIncidentsReturnsBadRequestOnInvalidCoordinates() {
    RadiusRequestDto request = new RadiusRequestDto(10.0, 20.0, 500);
    when(incidentUseCase.findAllInGivenRange(any(RadiusCommand.class)))
        .thenThrow(new InvalidCoordinatesException("invalid"));

    ResponseEntity<List<IncidentPreviewResponseDto>> response =
        controller.findNearbyIncidents(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void confirmIncidentReturnsDtoWhenAuthenticated() {
    User user = sampleUser();
    when(userService.isAuthenticated()).thenReturn(true);
    when(userService.getUser()).thenReturn(Optional.of(user));
    when(incidentUseCase.confirm(10L, user.uid())).thenReturn(incident);
    when(incidentResponseMapper.toIncidentDetailedResponseDto(incident)).thenReturn(detailedResponse);

    ResponseEntity<IncidentDetailedResponseDto> response = controller.confirmIncidentPresence(10L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(detailedResponse);
  }

  @Test
  void confirmIncidentReturnsConflictWhenUserNotAuthenticated() {
    when(userService.isAuthenticated()).thenReturn(false);

    ResponseEntity<IncidentDetailedResponseDto> response = controller.confirmIncidentPresence(10L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    verify(incidentUseCase, never()).confirm(any(Long.class), any(UserId.class));
  }

  @Test
  void denyIncidentReturnsConflictWhenAlreadyDenied() {
    User user = sampleUser();
    when(userService.isAuthenticated()).thenReturn(true);
    when(userService.getUser()).thenReturn(Optional.of(user));
    when(incidentUseCase.deny(10L, user.uid()))
        .thenThrow(new IncidentAlreadyConfirmedException("already denied"));

    ResponseEntity<IncidentDetailedResponseDto> response = controller.denyIncidentPresence(10L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void deleteExpiredIncidentHandlesScenarios() {
    ResponseEntity<Void> response = controller.deleteExpiredIncident(50L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(incidentUseCase).deleteIfExpired(50L);

    doThrow(new IncidentNotFoundException("missing"))
        .when(incidentUseCase).deleteIfExpired(60L);
    ResponseEntity<Void> missing = controller.deleteExpiredIncident(60L);
    assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    doThrow(new IncidentNotExpiredException("active"))
        .when(incidentUseCase).deleteIfExpired(61L);
    ResponseEntity<Void> active = controller.deleteExpiredIncident(61L);
    assertThat(active.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void deleteIncidentHandlesNotFound() {
    ResponseEntity<Void> response = controller.delete(99L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(incidentUseCase).deleteById(99L);

    doThrow(new IncidentNotFoundException("missing")).when(incidentUseCase).deleteById(100L);
    ResponseEntity<Void> missing = controller.delete(100L);
    assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteExpiredIncidentsCleanupEndpointReturnsNoContent() {
    ResponseEntity<Void> response = controller.deleteExpiredIncidents();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(incidentUseCase).deleteExpiredIncidents();
  }

  private IncidentRequestDto incidentRequest() {
    return IncidentRequestDto.builder()
        .title("title")
        .description("description")
        .files(sampleFiles())
        .lat(10.0)
        .lon(20.0)
        .build();
  }

  private Set<MultipartFile> sampleFiles() {
    MultipartFile file = new MockMultipartFile(
        "files", "photo.png", "image/png", "hello".getBytes());
    return Set.of(file);
  }

  private Incident sampleIncident() {
    return Incident.builder()
        .id(new IncidentId(42L))
        .userId(new UserId("uid-1"))
        .locationId(new LocationId(7L))
        .title("Road issue")
        .description("desc")
        .media(Set.of(new Media(1L, "photo.png", "image/png")))
        .sentimentEngagement(SentimentEngagement.builder().likes(1).dislikes(0).build())
        .engagementStats(new EngagementStats(0, 0, 0))
        .build();
  }

  private IncidentDetailedResponseDto sampleDetailedResponse() {
    return IncidentDetailedResponseDto.builder()
        .id(42L)
        .title("Road issue")
        .description("desc")
        .userUid("uid-1")
        .media(Set.of(new MediaDto("photo.png")))
        .confirm(0)
        .deny(0)
        .consecutiveDenies(0)
        .like(1)
        .dislike(0)
        .lat(10.0)
        .lon(20.0)
        .address("Main St")
        .build();
  }

  private IncidentPreviewResponseDto samplePreviewResponse() {
    return IncidentPreviewResponseDto.builder()
        .id(42L)
        .title("Road issue")
        .media(Set.of(new MediaDto("photo.png")))
        .lat(10.0)
        .lon(20.0)
        .build();
  }

  private User sampleUser() {
    return User.builder()
        .uid(new UserId("firebase-1"))
        .email("user@example.com")
        .name("User")
        .picture("pic")
        .role(Role.USER)
        .emailVerified(true)
        .deviceIdToken("token")
        .range(5)
        .build();
  }
}
