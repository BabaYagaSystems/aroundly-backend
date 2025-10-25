package com.backend.adapter.inbound.mapper.assembler;

import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import com.backend.adapter.inbound.mapper.IncidentMapper;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.Location;
import com.backend.port.outbound.repo.LocationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Assembles incident-related DTOs by enriching them
 * with location and actor details.
 */
@Component
@AllArgsConstructor
public class IncidentDtoAssembler {

  private final LocationRepository locationRepository;

  /**
   * Converts a domain {@link Incident} into a {@link IncidentDetailedResponseDto},
   * adding location coordinates, address, and actor username.
   *
   * @param incident the domain incident entity
   * @return a fully populated detailed response DTO
   */
  public IncidentDetailedResponseDto toDetailedDto(Incident incident) {
    Location location = locationRepository.findById(incident.getLocationId().value());
    double lat = location.latitude();
    double lon = location.longitude();
    String address = location.address();

    return IncidentDetailedResponseDto.builder()
        .title(incident.getTitle())
        .description(incident.getDescription())
//        .media(incident.getMedia())
        .confirm(incident.getEngagementStats().confirms())
        .deny(incident.getEngagementStats().denies())
        .consecutiveDenies(incident.getEngagementStats().consecutiveDenies())
        .like(incident.getSentimentEngagement().likes())
        .dislike(incident.getSentimentEngagement().dislikes())
        .address(address)
        .lat(lat)
        .lon(lon)
        .build();

  }
}
