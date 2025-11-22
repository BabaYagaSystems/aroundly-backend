package com.backend.adapter.outbound.mapper;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.adapter.outbound.repo.UserPersistenceRepository;
import com.backend.domain.actor.UserId;
import com.backend.domain.happening.Incident;
import com.backend.domain.happening.IncidentId;
import com.backend.domain.location.LocationId;
import com.backend.domain.reactions.EngagementStats;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class IncidentMapper {

  private final LocationPersistenceRepository locationPersistenceRepository;
  private final UserPersistenceRepository userPersistenceRepository;


  public IncidentEntity mapToEntity(Incident domain) {
    final LocationEntity locationEntity = findLocationByIncidentDomain(domain);
    final UserEntity userEntity = findUserByIncidentDomain(domain);

    IncidentEntity.IncidentEntityBuilder incidentEntityBuilder = IncidentEntity.builder()
        .user(userEntity)
        .title(domain.getTitle())
        .description(domain.getDescription())
        .location(locationEntity)
        .timePosted(domain.createdAt())
        .range(10)
        .confirms(domain.getEngagementStats().confirms())
        .denies(domain.getEngagementStats().denies())
        .expiresAt(domain.getExpiresAt());

    if (domain.getId() != null) {
      incidentEntityBuilder.id(domain.getId().value());
    }

    IncidentEntity incidentEntity = incidentEntityBuilder.build();

    domain.getMedia().stream()
      .map(MediaEntityMapper::toEntity)
      .forEach(incidentEntity::addMedia);

    return incidentEntity;
  }

  public Incident mapToDomain(IncidentEntity entity) {
    return Incident.builder()
      .id(new IncidentId(entity.getId()))
      .userId(new UserId(entity.getUser().getFirebaseUid()))
      .locationId(new LocationId(entity.getLocation().getId()))
      .media(entity.getMedia().stream()
          .map(MediaEntityMapper::toDomain)
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
    return userPersistenceRepository
        .findByFirebaseUid(incident.getUserId().value())
        .orElseThrow(() -> new IllegalStateException("User not found"));
  }
}
