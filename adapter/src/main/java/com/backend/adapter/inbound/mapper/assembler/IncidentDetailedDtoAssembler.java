package com.backend.adapter.inbound.mapper.assembler;

import com.backend.adapter.inbound.dto.media.MediaDto;
import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.adapter.outbound.factory.MediaPreviewFactory;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.port.inbound.ReactionUseCase;
import com.backend.port.inbound.commands.ReactToIncidentCommand;
import com.backend.port.outbound.repo.LocationRepository;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Assembles detailed incident response payloads by enriching the mapped incident with media,
 * location data, and engagement metrics coming from the reaction subsystem.
 */
@Component
@AllArgsConstructor
public class IncidentDetailedDtoAssembler {

  private final IncidentMapper mapper;
  private final LocationRepository locationRepository;
  private final MediaPreviewFactory mediaPreviewFactory;
  private final ReactionUseCase reactionUseCase;

  /**
   * Builds a detailed response DTO for the provided incident, including media previews, location
   * details, and the latest like/dislike counts.
   *
   * @param incident domain incident being exposed to the client
   * @return fully populated detailed response DTO
   */
  public IncidentDetailedResponseDto toDetailedDto(Incident incident) {
    IncidentDetailedResponseDto dto = mapper.toIncidentDetailedResponseDto(incident);
    Set<MediaDto> mediaDtos = mediaPreviewFactory.build(incident.getMedia());

    IncidentDetailedResponseDto.IncidentDetailedResponseDtoBuilder builder =
        dto.toBuilder().media(mediaDtos);

    Location location = locationRepository.findById(incident.getLocationId().value());

    if (location != null) {
      builder.lat(location.latitude()).lon(location.longitude()).address(location.address());
    }

//    var summary = reactionUseCase.getSummary(new ReactToIncidentCommand(incident.getId(), null));
//    builder
//        .like(summary.likes())
//        .dislike(summary.dislikes());

    return builder.build();
  }
}
