package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.mapper.MediaEntityMapper;
import com.backend.adapter.outbound.repo.*;
import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.LocationId;
import com.backend.domain.reactions.EngagementStats;
import com.backend.port.outbound.repo.IncidentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class IncidentPersistence implements IncidentRepository {

  private final IncidentPersistenceRepository incidentPersistenceRepository;
  private final LocationPersistenceRepository locationPersistenceRepository;

  private final MediaEntityMapper mediaEntityMapper;

  /**
     * Since the Happening is abstract it needs to be created by each child entity that implements it
     * Also it needs to be mapped manually since MapStruct is not working with abstract classes
     * */
    @Override
    public Incident save(Incident incident) {
      IncidentEntity incidentEntity = toEntityIncident(incident);

      incidentPersistenceRepository.save(incidentEntity);

      return incident;
    }

    @Override
    public Optional<Incident> findById(long id) {
        IncidentEntity incidentEntity = incidentPersistenceRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Incident not found"));

        return Optional.of(toDomainIncident(incidentEntity));
    }

    @Override
    public boolean existsById(long happeningId) {
      return incidentPersistenceRepository.existsById(happeningId);
    }

    @Override
    public List<Incident> findAllInGivenRange(double lat0, double lon0, double radiusMeters) {

        List<IncidentEntity> incidentEntities = incidentPersistenceRepository.findAllInGivenRange(lat0, lon0, radiusMeters);

      return incidentEntities.stream()
          .map(this::toDomainIncident)
          .toList();
    }

    private IncidentEntity toEntityIncident(Incident incident) {
      LocationEntity locationEntity = locationPersistenceRepository
        .findById(incident.getLocationId().value())
        .orElseThrow(() -> new IllegalStateException("Location not found"));

      IncidentEntity incidentEntity =  IncidentEntity.builder()
        .title(incident.getTitle())
        .description(incident.getDescription())
        .location(locationEntity)
        .timePosted(incident.createdAt())
        .range(1000) /// WHERE FROM DO WE NEED TO GET THIS VALUE
        .confirms(incident.getEngagementStats().confirms())
        .denies(incident.getEngagementStats().denies())
        .build();

      incident.getMedia().stream()
          .map(mediaEntityMapper::toEntity)
          .forEach(incidentEntity::addMedia);

      return incidentEntity;
    }

    private Incident toDomainIncident(IncidentEntity entity) {
      return Incident.builder()
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
        .build();
        }

    @Override
    public void deleteById(long id) {
        incidentPersistenceRepository.deleteById(id);
    }

    @Override
    public List<Incident> findByUserId(String userId) {
        return List.of();
    }
}
