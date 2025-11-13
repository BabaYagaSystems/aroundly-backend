package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.IncidentEngagementEntity;
import com.backend.adapter.outbound.repo.IncidentEngagementPersistenceRepository;
import com.backend.adapter.outbound.repo.IncidentPersistenceRepository;
import com.backend.domain.actor.UserId;
import com.backend.domain.reactions.IncidentEngagementType;
import com.backend.port.outbound.repo.IncidentEngagementRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class IncidentEngagementPersistence implements IncidentEngagementRepository {

  private final IncidentEngagementPersistenceRepository engagementRepository;
  private final IncidentPersistenceRepository incidentRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<IncidentEngagementType> findUserEngagement(long incidentId, UserId userId) {
    return engagementRepository.findByIncidentIdAndUserId(incidentId, userId.value())
        .map(IncidentEngagementEntity::getEngagementType);
  }

  @Override
  @Transactional
  public void saveEngagement(long incidentId, UserId userId, IncidentEngagementType type) {
    IncidentEngagementEntity entity = engagementRepository
        .findByIncidentIdAndUserId(incidentId, userId.value())
        .orElseGet(() -> IncidentEngagementEntity.builder()
            .incident(incidentRepository.getReferenceById(incidentId))
            .userId(userId.value())
            .build());

    entity.setEngagementType(type);
    entity.setEngagedAt(Instant.now());

    engagementRepository.save(entity);
  }
}
