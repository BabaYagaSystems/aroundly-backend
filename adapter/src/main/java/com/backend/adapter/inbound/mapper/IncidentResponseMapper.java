package com.backend.adapter.inbound.mapper;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.request.IncidentRequestDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.outbound.factory.MediaPreviewFactory;
import com.backend.adapter.outbound.repo.persistence.UserSyncService;
import com.backend.domain.actor.UserId;
import com.backend.domain.actor.User;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.UploadMediaCommand;
import com.backend.port.outbound.repo.LocationRepository;
import com.backend.services.AuthenticatedUserService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class IncidentResponseMapper {

  private final UserSyncService userSyncService;
  private final AuthenticatedUserService authenticatedUserService;
  private final LocationRepository locationRepository;
  private final MediaPreviewFactory mediaPreviewFactory;

  public CreateIncidentCommand toCreateIncidentCommand(IncidentRequestDto incidentRequestDto) {
    final Set<UploadMediaCommand> mediaCommands = toUploads(incidentRequestDto.files());

    return new CreateIncidentCommand(
        extractUserId(),
        incidentRequestDto.title(),
        incidentRequestDto.description(),
        mediaCommands,
        incidentRequestDto.lat(),
        incidentRequestDto.lon());
  }

  public IncidentDetailedResponseDto toIncidentDetailedResponseDto(Incident incident) {
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

  public IncidentPreviewResponseDto toIncidentPreviewResponseDto(Incident incident) {
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

  private Set<MediaDto> toMediaDto(Incident incident) {
    return mediaPreviewFactory.build(incident.getMedia());
  }

  private Location extractLocation(Incident incident) {
    return locationRepository.findById(incident.getLocationId().value());
  }

  private UserId extractUserId() {
    final User user = authenticatedUserService.requireCurrentUser();
    final String extractedUid = userSyncService.getOrCreateUser(user).getFirebaseUid();

    return new UserId(extractedUid);
  }

  /**
   * Converts multiple files to upload commands.
   *
   * @param files uploaded files
   * @return upload commands
   */
  private static Set<UploadMediaCommand> toUploads(Set<MultipartFile> files) {
    if (files == null || files.isEmpty()) return Set.of();

    return files.stream()
        .map(IncidentResponseMapper::toUpload)
        .collect(Collectors.toSet());
  }

  private static UploadMediaCommand toUpload(MultipartFile file) {
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

  private static String sanitizeFilename(String name) {
    if (name == null || name.isBlank()) return "file";
    return name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private static String safeContentType(String ct) {
    return (ct == null || ct.isBlank()) ? "application/octet-stream" : ct;
  }

}
