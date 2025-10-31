package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.LocationId;
import com.backend.domain.reactions.EngagementStats;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class IncidentMapper {

  private final MediaEntityMapper mediaEntityMapper;
  private final LocationPersistenceRepository locationPersistenceRepository;

  public IncidentEntity mapToEntity(Incident domain) {
    final LocationEntity locationEntity = findLocationByIncidentDomain(domain);

    IncidentEntity incidentEntity = IncidentEntity.builder()
      .id(domain.getId())
      .title(domain.getTitle())
      .description(domain.getDescription())
      .location(locationEntity)
      .timePosted(domain.createdAt())
      .range(1000)
      .confirms(domain.getEngagementStats().confirms())
      .denies(domain.getEngagementStats().denies())
      .expiresAt(domain.getExpiresAt())
      .build();

    domain.getMedia().stream()
      .map(mediaEntityMapper::toEntity)
      .forEach(incidentEntity::addMedia);

    return incidentEntity;
  }

  public Incident mapToDomain(IncidentEntity entity) {
    return Incident.builder()
      .id(entity.getId())
      .actorId(new ActorId("abc"))
      .locationId(new LocationId(entity.getLocation().getId()))
      .media(entity.getMedia().stream()
          .map(mediaEntityMapper::toDomain)
          .collect(Collectors.toSet()))
      .title(entity.getTitle())
      .description(entity.getDescription())
      .engagementStats(new EngagementStats(
          entity.getConfirms(),
          entity.getDenies(),
          entity.getDenies()))
      .expiresAt(entity.getExpiresAt())
      .build();
  }

  private LocationEntity findLocationByIncidentDomain(Incident incident) {
    return locationPersistenceRepository
      .findById(incident.getLocationId().value())
      .orElseThrow(() -> new IllegalStateException("Location not found"));
  }
}
