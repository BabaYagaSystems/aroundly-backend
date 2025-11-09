package com.backend.adapter.inbound.rest;

import com.backend.adapter.inbound.dto.request.IncidentRequestDto;
import com.backend.adapter.inbound.dto.request.RadiusRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.adapter.inbound.mapper.LocationMapper;
import com.backend.adapter.inbound.mapper.assembler.IncidentDetailedDtoAssembler;
import com.backend.adapter.inbound.mapper.assembler.IncidentPreviewDtoAssembler;
import com.backend.adapter.inbound.rest.exception.incident.ActorNotFoundException;
import com.backend.adapter.inbound.rest.exception.incident.DuplicateIncidentException;
import com.backend.adapter.inbound.rest.exception.incident.IncidentAlreadyConfirmedException;
import com.backend.adapter.inbound.rest.exception.incident.IncidentNotExpiredException;
import com.backend.adapter.inbound.rest.exception.incident.IncidentNotFoundException;
import com.backend.adapter.inbound.rest.exception.incident.InvalidCoordinatesException;
import com.backend.adapter.outbound.repo.persistence.UserSyncService;
import com.backend.domain.actor.ActorId;
import com.backend.domain.actor.User;
import com.backend.domain.happening.Incident;
import com.backend.port.inbound.IncidentUseCase;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.RadiusCommand;
import com.backend.services.AuthenticatedUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing CRUD and query endpoints for {@link Incident} resources.
 * Provides endpoints for creation, updates, previews, detailed views, engagement actions,
 * and deletion of incidents.
 */
@RestController
@Validated
@Slf4j
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "IncidentEntity manipulation endpoints")
public class IncidentController {

  private final IncidentUseCase incidentUseCase;
  private final IncidentDetailedDtoAssembler incidentDetailedResponseAssembler;
  private final IncidentMapper incidentMapper;
  private final LocationMapper locationMapper;
  private final IncidentPreviewDtoAssembler incidentPreviewDtoAssembler;
  private final UserSyncService userSyncService;
  private final AuthenticatedUserService tokenValidationService;

  public IncidentController(
      IncidentUseCase incidentUseCase,
      IncidentDetailedDtoAssembler incidentDetailedResponseAssembler,
      IncidentMapper incidentMapper,
      LocationMapper locationMapper,
      IncidentPreviewDtoAssembler incidentPreviewDtoAssembler, UserSyncService userSyncService,
      AuthenticatedUserService tokenValidationService) {

    this.incidentUseCase = incidentUseCase;
    this.incidentMapper = incidentMapper;
    this.incidentDetailedResponseAssembler = incidentDetailedResponseAssembler;
    this.locationMapper = locationMapper;
    this.incidentPreviewDtoAssembler = incidentPreviewDtoAssembler;
    this.userSyncService = userSyncService;
    this.tokenValidationService = tokenValidationService;
  }

  /**
   * Creates a new incident.
   *
   * @param incidentRequestDto DTO containing incident data
   * @return detailed DTO of the created incident
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Creates an incident",
      description = "Handles a user request to create an incident and returns details"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "IncidentEntity created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "IncidentEntity already exists")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<IncidentDetailedResponseDto> create(
      @ModelAttribute @Valid IncidentRequestDto incidentRequestDto) {

    try {
      User user = tokenValidationService.requireCurrentUser();
      final CreateIncidentCommand createIncidentCommand = incidentMapper.toCreateIncidentCommand(incidentRequestDto);

      ActorId actorId = new ActorId(userSyncService.getOrCreateUser(user).getFirebaseUid());
      final CreateIncidentCommand createIncidentCommandWithActor = createIncidentCommand.toBuilder()
          .actorId(actorId)
          .build();

      Incident incident = incidentUseCase.create(createIncidentCommandWithActor);
      IncidentDetailedResponseDto incidentDetailedResponseDto = incidentDetailedResponseAssembler.toDetailedDto(incident);

      return new ResponseEntity<>(incidentDetailedResponseDto, HttpStatus.CREATED);

    } catch (DuplicateIncidentException e) {
      log.warn("Attempted to create duplicate incident: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (ValidationException e) {
      log.warn("Invalid incident data: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Updates an existing incident.
   *
   * @param id                   identifier of the incident
   * @param newIncidentRequestDto DTO with updated incident data
   * @return detailed DTO of the updated incident
   */
  @PatchMapping("/{id}")
  @Operation(
      summary = "Updates an existing incident",
      description = "Handles a user request to update an incident and returns updated details"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "IncidentEntity updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "IncidentEntity not found")
  })
  public ResponseEntity<IncidentDetailedResponseDto> update(
      @PathVariable long id,
      @RequestBody @Valid IncidentRequestDto newIncidentRequestDto) {

    try {
      CreateIncidentCommand newCreateIncidentCommand = incidentMapper
          .toCreateIncidentCommand(newIncidentRequestDto);

      Incident updatedIncident = incidentUseCase.update(id, newCreateIncidentCommand);
      IncidentDetailedResponseDto incidentDetailedResponseDto = incidentDetailedResponseAssembler.toDetailedDto(updatedIncident);

      return ResponseEntity.ok(incidentDetailedResponseDto);
    } catch (IncidentNotFoundException e) {
      log.warn("IncidentEntity not found for update: {}", id);
      return ResponseEntity.notFound().build();
    } catch (ValidationException e) {
      log.warn("Invalid update data for incident {}: {}", id, e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Retrieves a preview representation of an incident.
   *
   * @param id identifier of the incident
   * @return preview DTO of the incident
   */
  @GetMapping("/{id}/preview")
  @Operation(
      summary = "Provides a preview of an incident",
      description = "Provides a limited about of data from an incident. Best used for a preview of incident, rather than providing "
          + "the entire list of information."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "IncidentEntity preview retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "IncidentEntity not found")
  })
  public ResponseEntity<IncidentPreviewResponseDto> getIncidentInPreview(@PathVariable long id) {
    try {
      Incident incident = incidentUseCase.findById(id);
      IncidentPreviewResponseDto incidentPreviewResponseDto =
          incidentPreviewDtoAssembler.toPreviewDto(incident);

      return ResponseEntity.ok(incidentPreviewResponseDto);
    } catch (IncidentNotFoundException e) {
      log.warn("IncidentEntity not found: {}", id);
      return ResponseEntity.badRequest().build();
    } catch (ClassCastException e) {
      log.error("Found happening is not an incident: {}", id);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Retrieves a detailed representation of an incident.
   *
   * @param id identifier of the incident
   * @return detailed DTO of the incident
   */
  @GetMapping("/{id}/details")
  @Operation(
      summary = "Provides a detailed incident",
      description = "Provides a detailed form of an incident. Used when the incident is open in separate page."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "IncidentEntity details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "IncidentEntity not found")
  })
  public ResponseEntity<IncidentDetailedResponseDto> getIncidentInDetails(@PathVariable long id) {
    try {
      Incident incident = incidentUseCase.findById(id);
      IncidentDetailedResponseDto incidentDetailedResponseDto = incidentDetailedResponseAssembler.toDetailedDto(incident);

      return ResponseEntity.ok(incidentDetailedResponseDto);
    } catch (IncidentNotFoundException e) {
      log.warn("IncidentEntity not found: {}", id);
      return ResponseEntity.badRequest().build();
    } catch (ClassCastException e) {
      log.error("Found happening is not an incident: {}", id);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Finds preview representations of incidents reported by a given actor.
   *
   * @param id actor identifier
   * @return list of preview DTOs
   */
  @GetMapping("/detailed/actor/{id}")
  @Operation(
      summary = "Incidents of an actor",
      description = "Finds all incidents posted by a specific actor (in preview)"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Actor incidents retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid actor ID")
  })
  public ResponseEntity<List<IncidentPreviewResponseDto>> findActorIncidentsInPreview(
      @PathVariable String id) {

    try {
      List<Incident> incidents = incidentUseCase.findByUserId(id);
      List<IncidentPreviewResponseDto> incidentPreviewResponseDtos = incidents.stream()
          .map(incidentPreviewDtoAssembler::toPreviewDto)
          .toList();

      return ResponseEntity.ok(incidentPreviewResponseDtos);
    } catch (ActorNotFoundException e) {
      log.warn("Actor not found: {}", id);
      return ResponseEntity.ok(Collections.emptyList());
    }
  }

  /**
   * Finds incidents located within the specified radius.
   *
   * @param radiusRequestDto DTO containing center coordinates and radius
   * @return list of incident previews within range
   */
  @GetMapping("/nearby")
  @Operation(
      summary = "Incidents nearby user",
      description = "Finds all incidents in user's setup range"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Nearby incidents retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid coordinates or radius")
  })
  public ResponseEntity<List<IncidentPreviewResponseDto>> findNearbyIncidents(
      @ModelAttribute @Valid RadiusRequestDto radiusRequestDto) {

    try {
      RadiusCommand radiusCommand = locationMapper.toRadiusCommand(radiusRequestDto);
      List<Incident> incidents = incidentUseCase.findAllInGivenRange(radiusCommand);
      List<IncidentPreviewResponseDto> responseDtos = incidents.stream()
          .map(incidentPreviewDtoAssembler::toPreviewDto)
          .toList();


      return ResponseEntity.ok(responseDtos);
    } catch (InvalidCoordinatesException e) {
      log.warn("Invalid coordinates provided: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Confirms the presence of an incident (engagement action).
   *
   * @param id incident identifier
   * @return updated detailed DTO
   */
  @PostMapping("/{id}/engagement/confirm")
  @Operation(
      summary = "Confirms the presence of incident",
      description = "If user is in the range of event, he can confirm if the incident still exist"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "IncidentEntity confirmed successfully"),  // Changed from 201
      @ApiResponse(responseCode = "404", description = "IncidentEntity not found"),
      @ApiResponse(responseCode = "409", description = "IncidentEntity already confirmed")
  })
  public ResponseEntity<IncidentDetailedResponseDto> confirmIncidentPresence(@PathVariable long id) {
    try {
      User user = getUserAuthentication().get();

      Incident incident = incidentUseCase.confirm(id, userSyncService.getOrCreateUser(user).getId());
      IncidentDetailedResponseDto incidentDetailedResponseDto = incidentDetailedResponseAssembler.toDetailedDto(incident);

      return ResponseEntity.ok(incidentDetailedResponseDto);
    } catch (IncidentNotFoundException e) {
      log.warn("IncidentEntity not found for confirmation: {}", id);
      return ResponseEntity.notFound().build();
    } catch (IncidentAlreadyConfirmedException e) {
      log.warn("IncidentEntity already confirmed: {}", id);
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  /**
   * Denies the presence of an incident (engagement action).
   *
   * @param id incident identifier
   * @return updated detailed DTO
   */
  @PostMapping("/{id}/engagement/deny")
  @Operation(
      summary = "Denies the presence of incident",
      description = "If user is in the range of event, he can deny if the incident is not relevant anymore"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "IncidentEntity denied successfully"),
      @ApiResponse(responseCode = "404", description = "IncidentEntity not found"),
      @ApiResponse(responseCode = "409", description = "IncidentEntity already denied")
  })
  public ResponseEntity<IncidentDetailedResponseDto> denyIncidentPresence(@PathVariable long id) {
    try {
      User user = getUserAuthentication().get();
      Incident incident = incidentUseCase.deny(id, userSyncService.getOrCreateUser(user).getId());
      IncidentDetailedResponseDto incidentDetailedResponseDto = incidentDetailedResponseAssembler.toDetailedDto(incident);

      return ResponseEntity.ok(incidentDetailedResponseDto);
    } catch (IncidentNotFoundException e) {
      log.warn("IncidentEntity not found for denial: {}", id);
      return ResponseEntity.notFound().build();
    } catch (IncidentAlreadyConfirmedException e) {
      log.warn("IncidentEntity already denied: {}", id);
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  /**
   * Deletes an incident if it has expired.
   *
   * @param id incident identifier
   * @return 204 No Content
   */
  @DeleteMapping("/{id}/expired")
  @Operation(
      summary = "Deletes expired incident",
      description = "If the incident reached 3 consecutive denies, therefore it's confirmed to be deleted"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Expired incident deleted successfully"),
      @ApiResponse(responseCode = "404", description = "IncidentEntity not found"),
      @ApiResponse(responseCode = "400", description = "IncidentEntity has not expired")
  })
  public ResponseEntity<Void> deleteExpiredIncident(@PathVariable long id) {
    try {
      incidentUseCase.deleteIfExpired(id);

      return ResponseEntity.noContent().build();
    } catch (IncidentNotFoundException e) {
      log.warn("IncidentEntity not found for expiry deletion: {}", id);
      return ResponseEntity.notFound().build();
    } catch (IncidentNotExpiredException e) {
      log.warn("Attempted to delete non-expired incident: {}", id);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Deletes an incident by its identifier.
   *
   * @param id incident identifier
   * @return 204 No Content
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Deletes the incident",
      description = "yea...as summary says"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "IncidentEntity deleted successfully"),
      @ApiResponse(responseCode = "404", description = "IncidentEntity not found")
  })
  public ResponseEntity<Void> delete(@PathVariable long id) {
    try {
      incidentUseCase.deleteById(id);

      return ResponseEntity.noContent().build();
    } catch (IncidentNotFoundException e) {
      log.warn("IncidentEntity not found for deletion: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  private Optional<User> getUserAuthentication() {
    try {
      if (tokenValidationService.isAuthenticated()) {
        return Optional.of(tokenValidationService.requireCurrentUser());
      }
    } catch (IllegalArgumentException e) {
      log.error("Missing or malformed Authorization header");
    }

    return Optional.empty();
  }
}
