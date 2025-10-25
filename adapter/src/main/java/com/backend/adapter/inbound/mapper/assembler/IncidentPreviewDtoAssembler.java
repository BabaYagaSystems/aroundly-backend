package com.backend.adapter.inbound.mapper.assembler;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentPreviewResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.adapter.outbound.factory.MediaPreviewFactory;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Assembles preview DTOs for incidents by enriching them with
 * location coordinates and media previews.
 */
@Component
@AllArgsConstructor
public class IncidentPreviewDtoAssembler {

  private final IncidentMapper mapper;
  private final LocationRepository locationRepository;
  private final MediaPreviewFactory mediaPreviewFactory;

  /**
   * Converts a domain {@link Incident} into a {@link IncidentPreviewResponseDto} enriched with
   * latitude, longitude, and media preview data.
   *
   * @param incident the source incident entity
   * @return the assembled preview DTO
   */
  public IncidentPreviewResponseDto toPreviewDto(Incident incident) {
    IncidentPreviewResponseDto dto = mapper.toIncidentPreviewResponseDto(incident);

    Set<MediaDto> previewMedia = mediaPreviewFactory.build(incident.getMedia());

    IncidentPreviewResponseDto.IncidentPreviewResponseDtoBuilder builder =
        dto.toBuilder().media(previewMedia);

    Location location = locationRepository.findById(incident.getLocationId().value());
    if (location != null) {
      builder.lat(location.latitude()).lon(location.longitude());
    }

    return builder.build();
  }
}
