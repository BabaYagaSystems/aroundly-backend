package com.backend.port.inbound.commands;

import java.util.Set;

/**
 * Command object used for creating a new Incident.
 *
 * @param title       the title of the incident
 * @param description the description of the incident
 * @param media       the set of media associated with the incident
 * @param lat         the latitude where the incident occurred
 * @param lon         the longitude where the incident occurred
 */
public record CreateIncidentCommand(
//        Long id,
    String title,
    String description,
    Set<UploadMediaCommand> media,
    double lat,
    double lon) { }
