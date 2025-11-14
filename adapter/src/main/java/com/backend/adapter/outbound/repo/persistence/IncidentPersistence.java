package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.mapper.IncidentMapper;
import com.backend.adapter.outbound.repo.*;
import com.backend.domain.happening.Incident;
import com.backend.port.outbound.repo.IncidentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IncidentPersistence implements IncidentRepository {

  private final IncidentPersistenceRepository incidentPersistenceRepository;
  private final IncidentMapper incidentMapper;

  @Override
  public Incident save(Incident incident) {
    IncidentEntity incidentEntity = incidentMapper.mapToEntity(incident);
    IncidentEntity savedEntity = incidentPersistenceRepository.save(incidentEntity);

    return incidentMapper.mapToDomain(savedEntity);
  }

  @Override
  public Optional<Incident> findById(long id) {
    IncidentEntity incidentEntity = incidentPersistenceRepository.findById(id)
      .orElseThrow(() -> new IllegalStateException("Incident not found"));

    return Optional.of(incidentMapper.mapToDomain(incidentEntity));
  }

  @Override
  public boolean existsById(long happeningId) {
    return incidentPersistenceRepository.existsById(happeningId);
  }

  @Override
  public List<Incident> findAllInGivenRange(double lat0, double lon0, double radiusMeters) {

    List<IncidentEntity> incidentEntities = incidentPersistenceRepository.findAllInGivenRange(lat0, lon0, radiusMeters);

    return incidentEntities.stream()
      .map(incidentMapper::mapToDomain)
      .toList();
  }

  @Override
  public void deleteById(long id) {
      incidentPersistenceRepository.deleteById(id);
  }

  @Override
  public List<Incident> findByUserId(String userId) {
      return incidentPersistenceRepository.findByUserFirebaseUid(userId).stream()
          .map(incidentMapper::mapToDomain)
          .toList();
  }
}
