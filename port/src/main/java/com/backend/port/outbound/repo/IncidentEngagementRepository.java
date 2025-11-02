package com.backend.port.outbound.repo;

import com.backend.domain.reactions.IncidentEngagementType;
import java.util.Optional;

public interface IncidentEngagementRepository {
  Optional<IncidentEngagementType> findUserEngagement(long incidentId, long userId);
  void saveEngagement(long incidentId, long userId, IncidentEngagementType type);
}
