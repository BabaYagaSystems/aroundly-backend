package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.adapter.outbound.repo.UserRepository;
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
  private final UserRepository userRepository;


  public IncidentEntity mapToEntity(Incident domain) {
    final LocationEntity locationEntity = findLocationByIncidentDomain(domain);
    final UserEntity userEntity = findUserByIncidentDomain(domain);

    IncidentEntity incidentEntity = IncidentEntity.builder()
      .id(domain.getId())
      .client(userEntity)
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
      .actorId(new ActorId(entity.getClient().getFirebaseUid()))
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

  private UserEntity findUserByIncidentDomain(Incident incident) {
    return userRepository
        .findByFirebaseUid(incident.getActorId().value())
        .orElseThrow(() -> new IllegalStateException("User not found"));
  }
}
