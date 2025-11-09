package com.backend.adapter.inbound.dto.response.incident;

import com.backend.adapter.inbound.dto.media.MediaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;

/**
 * Response DTO carrying detailed information about an incident.
 *
 * @param title              short title of the incident
 * @param description        detailed description of the incident
 * @param userUid      username of the user who reported/created the incident
 * @param media              related media (images, videos, etc.)
 * @param confirm            number of confirmations
 * @param deny               number of denials
 * @param consecutiveDenies  consecutive denial count
 * @param like               number of likes
 * @param dislike            number of dislikes
 * @param lat                latitude of the incident location
 * @param lon                longitude of the incident location
 * @param address            human-readable formatted address of the incident
 */
@Schema(description = "Detailed incident information including location, media, and community engagement metrics")
@Builder(toBuilder = true)
public record IncidentDetailedResponseDto(
    long id,

    @Schema(
        description = "Short descriptive title of the incident",
        example = "Road closure due to construction"
    )
    String title,

    @Schema(
        description = "Detailed description of the incident",
        example = "Main street is closed from 9 AM to 5 PM due to water pipe maintenance work"
    )
    String description,

    @Schema(
        description = "Username of the user who reported the incident",
        example = "john_doe"
    )
    String userUid,

    @Schema(
        description = "Collection of related media files (images, videos, etc.)"
    )
    Set<MediaDto> media,

    @Schema(
        description = "Number of user confirmations for this incident",
        example = "12"
    )
    int confirm,

    @Schema(
        description = "Number of user denials for this incident",
        example = "2"
    )
    int deny,

    @Schema(
        description = "Count of consecutive denials received",
        example = "0"
    )
    int consecutiveDenies,

    @Schema(
        description = "Number of likes received for this incident",
        example = "8"
    )
    int like,

    @Schema(
        description = "Number of dislikes received for this incident",
        example = "1"
    )
    int dislike,

    @Schema(
        description = "Latitude coordinate of the incident location in decimal degrees",
        example = "45.4642"
    )
    double lat,

    @Schema(
        description = "Longitude coordinate of the incident location in decimal degrees",
        example = "9.1900"
    )
    double lon,

    @Schema(
        description = "Human-readable formatted address of the incident location",
        example = "Via del Corso, 20121 Milan, Italy"
    )
    String address,

    @Schema(
        description = "Describes when the incident was created",
        example = "created at: 12:10PM"
    )
    Instant createdAt) { }