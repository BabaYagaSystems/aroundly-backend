package com.backend.adapter.inbound.mapper;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.request.IncidentRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.outbound.factory.MediaPreviewFactory;
import com.backend.services.UserService;
import com.backend.domain.actor.UserId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.UploadMediaCommand;
import com.backend.port.outbound.repo.LocationRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Maps inbound HTTP DTOs into application commands and domain models back into response DTOs
 * for incident-related endpoints. Handles user extraction, media transformation, and
 * location lookups to keep controllers thin.
 */
@Component
@RequiredArgsConstructor
public class IncidentResponseMapper {

  private final UserService userService;
  private final LocationRepository locationRepository;
  private final MediaPreviewFactory mediaPreviewFactory;

  /**
   * Converts an incident creation request into a command understood by the application layer.
   *
   * @param incidentRequestDto payload received from the client
   * @return command including authenticated user id, metadata, and media uploads
   */
  public CreateIncidentCommand toCreateIncidentCommand(final IncidentRequestDto incidentRequestDto) {
    final Set<UploadMediaCommand> mediaCommands = toUploads(incidentRequestDto.files());

    return new CreateIncidentCommand(
        extractUserId(),
        incidentRequestDto.title(),
        incidentRequestDto.description(),
        mediaCommands,
        incidentRequestDto.lat(),
        incidentRequestDto.lon());
  }

  /**
   * Builds the detailed incident response view, including media, stats, and location data.
   *
   * @param incident domain incident
   * @return DTO returned by the incident detail endpoint
   */
  public IncidentDetailedResponseDto toIncidentDetailedResponseDto(final Incident incident) {
    final Location location = extractLocation(incident);
    final Set<MediaDto> mediaDtos = toMediaDto(incident);

    return IncidentDetailedResponseDto.builder()
        .id(incident.getId().value())
        .userUid(incident.getUserId().value())
        .title(incident.getTitle())
        .description(incident.getDescription())
        .media(mediaDtos)
        .address(location.address())
        .lat(location.latitude())
        .lon(location.longitude())
        .like(incident.getSentimentEngagement().likes())
        .dislike(incident.getSentimentEngagement().dislikes())
        .confirm(incident.getEngagementStats().confirms())
        .deny(incident.getEngagementStats().denies())
        .consecutiveDenies(incident.getEngagementStats().consecutiveDenies())
        .createdAt(incident.createdAt())
        .build();
  }

  /**
   * Builds a lightweight incident preview response used for list endpoints.
   *
   * @param incident domain incident
   * @return DTO containing basic info suitable for feed rendering
   */
  public IncidentPreviewResponseDto toIncidentPreviewResponseDto(final Incident incident) {
    final Set<MediaDto> mediaDtos = toMediaDto(incident);
    final Location location = extractLocation(incident);

    return IncidentPreviewResponseDto.builder()
        .id(incident.getId().value())
        .title(incident.getTitle())
        .media(mediaDtos)
        .lat(location.latitude())
        .lon(location.longitude())
        .createdAt(incident.createdAt())
        .build();
  }

  /**
   * Converts the incident's media attachments into DTOs using the preview factory.
   */
  private Set<MediaDto> toMediaDto(final Incident incident) {
    return mediaPreviewFactory.build(incident.getMedia());
  }

  /**
   * Resolves the location entity referenced by the incident.
   */
  private Location extractLocation(final Incident incident) {
    return locationRepository.findById(incident.getLocationId().value());
  }

  /**
   * Pulls the authenticated user's id from the security context.
   */
  private UserId extractUserId() {
    final String extractedUid = userService.getUser().get().uid().value();

    return new UserId(extractedUid);
  }

  /**
   * Converts multiple files to upload commands.
   *
   * @param files uploaded files
   * @return upload commands
   */
  private static Set<UploadMediaCommand> toUploads(final Set<MultipartFile> files) {
    if (files == null || files.isEmpty()) return Set.of();

    return files.stream()
        .map(IncidentResponseMapper::toUpload)
        .collect(Collectors.toSet());
  }

  /**
   * Converts a single multipart file into an upload command, sanitizing metadata and guarding IO.
   */
  private static UploadMediaCommand toUpload(final MultipartFile file) {
    try {
      return new UploadMediaCommand(
          file.getInputStream(),
          sanitizeFilename(file.getOriginalFilename()),
          file.getSize(),
          safeContentType(file.getContentType()));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read upload: " + file.getOriginalFilename(), e);
    }
  }

  /**
   * Provides a safe filename that strips characters unsupported by storage providers.
   */
  private static String sanitizeFilename(final String name) {
    if (name == null || name.isBlank()) return "file";
    return name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  /**
   * Ensures uploads always carry a content type, defaulting to octet-stream when missing.
   */
  private static String safeContentType(String ct) {
    return (ct == null || ct.isBlank()) ? "application/octet-stream" : ct;
  }

}
