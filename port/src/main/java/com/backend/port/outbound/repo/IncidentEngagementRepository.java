package com.backend.port.outbound.repo;

import com.backend.domain.actor.UserId;
import com.backend.domain.reactions.IncidentEngagementType;
import java.util.Optional;

public interface IncidentEngagementRepository {
  Optional<IncidentEngagementType> findUserEngagement(long incidentId, UserId userId);
  void saveEngagement(long incidentId, UserId userId, IncidentEngagementType type);
}
