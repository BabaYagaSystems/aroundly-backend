package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.HappeningEntity;
import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.entity.MediaEntity;
import com.backend.adapter.outbound.mapper.MediaEntityMapper;
import com.backend.adapter.outbound.repo.*;
import com.backend.domain.actor.ActorId;
import com.backend.domain.happening.Incident;
import com.backend.domain.location.LocationId;
import com.backend.domain.reactions.EngagementStats;
import com.backend.port.outbound.repo.IncidentRepository;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class IncidentPersistence implements IncidentRepository {

  private final IncidentPersistenceRepository incidentPersistenceRepository;
  private final HappeningPersistenceRepository happeningPersistenceRepository;
  private final LocationPersistenceRepository locationPersistenceRepository;

  private final MediaEntityMapper mediaEntityMapper;

  /**
     * Since the Happening is abstract it needs to be created by each child entity that implements it
     * Also it needs to be mapped manually since MapStruct is not working with abstract classes
     * */
    @Override
    public Incident save(Incident incident) {
      IncidentEntity incidentEntity = toEntityIncident(incident);
      HappeningEntity happeningEntity = incidentEntity.getHappening();

      for (MediaEntity mediaEntity : happeningEntity.getMedia()) {
          mediaEntity.setHappeningEntity(happeningEntity);
      }

      happeningPersistenceRepository.save(happeningEntity);
      incidentPersistenceRepository.save(incidentEntity);

      return incident;
    }

    @Override
    public Optional<Incident> findById(long id) {
        IncidentEntity incidentEntity = incidentPersistenceRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Location not found"));

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
          .findById(incident.locationId().value())
          .orElseThrow(() -> new IllegalStateException("Location not found"));

      Set<MediaEntity> mediaEntities = incident.media().stream()
          .map(mediaEntityMapper::toEntity)
          .collect(Collectors.toSet());

      HappeningEntity happeningEntity = HappeningEntity.builder()
          .title(incident.getTitle())
          .description(incident.getDescription())
          .media(mediaEntities)
          .createdAt(LocalDateTime.now())
          .location(locationEntity)
          .build();

      return IncidentEntity.builder()
          .happening(happeningEntity)
          .timePosted(LocalDateTime.now())
          .range(1000) /// WHERE FROM DO WE NEED TO GET THIS VALUE
          .confirms(incident.getEngagementStats().confirms())
          .denies(incident.getEngagementStats().denies())
          .build();
    }

    private Incident toDomainIncident(IncidentEntity entity) {
        HappeningEntity happening = entity.getHappening();
        LocationEntity location = happening.getLocation();

        return Incident.builder()
            .actorId(new ActorId("abc"))
            .locationId(new LocationId(location.getId()))
            .media(happening.getMedia().stream()
                .map(mediaEntityMapper::toDomain)
                .collect(Collectors.toSet()))
            .title(happening.getTitle())
            .description(happening.getDescription())
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
