package com.backend.adapter.inbound.websocket;

import com.backend.adapter.inbound.dto.response.incident.IncidentDetailedResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes incident events to connected WebSocket subscribers using the configured topics.
 * <p>
 * Intended to be invoked from REST/controllers after an incident is created or updated so
 * real-time clients can refresh immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentBroadcast {

  private final SimpMessagingTemplate template;

  /**
   * Broadcasts a newly created incident to all clients subscribed to {@code /topic/incident/new}.
   *
   * @param incidentDetailedResponseDto incident payload sent to the subscribers
   */
  public void broadcastCreatedIncident(IncidentDetailedResponseDto incidentDetailedResponseDto) {
    template.convertAndSend("/topic/incident/new", incidentDetailedResponseDto);

    log.info("Incident {} is broadcasted", incidentDetailedResponseDto);
  }

}
