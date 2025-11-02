package com.backend.adapter.outbound.repo.redis;

import org.springframework.stereotype.Component;

/**
 * Centralises Redis key naming for incident reaction sets to ensure consistent reads/writes.
 */
@Component
public class ReactionKeyBuilder {
  private static final String INCIDENT_NAMESPACE = "incident";

  public String likesKey(long incidentId) {
    return INCIDENT_NAMESPACE + ":" + incidentId + ":likes";
  }

  public String dislikesKey(long incidentId) {
    return INCIDENT_NAMESPACE + ":" + incidentId + ":dislikes";
  }
}
