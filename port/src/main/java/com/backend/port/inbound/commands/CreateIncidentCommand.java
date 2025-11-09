package com.backend.port.inbound.commands;

import com.backend.domain.actor.ActorId;
import java.util.Set;
import lombok.Builder;

/**
 * Command object used for creating a new Incident.
 *
 * @param title       the title of the incident
 * @param description the description of the incident
 * @param media       the set of media associated with the incident
 * @param lat         the latitude where the incident occurred
 * @param lon         the longitude where the incident occurred
 */
@Builder(toBuilder = true)
public record CreateIncidentCommand(
    ActorId actorId,
    String title,
    String description,
    Set<UploadMediaCommand> media,
    double lat,
    double lon) { }
