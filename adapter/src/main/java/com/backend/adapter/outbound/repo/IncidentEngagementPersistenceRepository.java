package com.backend.adapter.outbound.repo;

import com.backend.adapter.outbound.entity.IncidentEngagementEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEngagementPersistenceRepository extends
    JpaRepository<IncidentEngagementEntity, Long> {

  Optional<IncidentEngagementEntity> findByIncident_IdAndUserId(long incidentId, long userId);
}
