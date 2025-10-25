package com.backend.adapter.inbound.mapper.assembler;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.adapter.outbound.factory.MediaPreviewFactory;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IncidentDetailedDtoAssembler {

  private final IncidentMapper mapper;
  private final LocationRepository locationRepository;
  private final MediaPreviewFactory mediaPreviewFactory;

  public IncidentDetailedResponseDto toDetailedDto(Incident incident) {
    IncidentDetailedResponseDto dto = mapper.toIncidentDetailedResponseDto(incident);
    Set<MediaDto> mediaDtos = mediaPreviewFactory.build(incident.getMedia());

    IncidentDetailedResponseDto.IncidentDetailedResponseDtoBuilder builder =
        dto.toBuilder().media(mediaDtos);

    Location location = locationRepository.findById(incident.getLocationId().value());

    if (location != null) {
      builder.lat(location.latitude()).lon(location.longitude()).address(location.address());
    }

    return builder.build();
  }
}
