package com.backend.adapter.outbound.repo;

import com.backend.adapter.outbound.entity.IncidentEngagementEntity;
import com.backend.domain.actor.UserId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEngagementPersistenceRepository extends
    JpaRepository<IncidentEngagementEntity, Long> {

  Optional<IncidentEngagementEntity> findByIncidentIdAndUserId(long incidentId, String userId);
}
