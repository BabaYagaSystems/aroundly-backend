package com.backend.services.scheduling;

import com.backend.port.inbound.IncidentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically removes incidents whose expiration timestamp is in the past by
 * delegating to {@link IncidentUseCase#deleteExpiredIncidents()}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentCleanupScheduler {

  private final IncidentUseCase incidentUseCase;

  /**
   * Runs at a fixed delay to ensure expired incidents get purged even when
   * no explicit DELETE endpoint is triggered.
   */
  @Transactional
  @Scheduled(fixedDelayString = "${incidents.cleanup.delay-ms:60000}")
  public void purgeExpiredIncidents() {
    try {
      incidentUseCase.deleteExpiredIncidents();
    } catch (Exception ex) {
      log.error("Failed to execute expired incident cleanup", ex);
    }
  }
}
