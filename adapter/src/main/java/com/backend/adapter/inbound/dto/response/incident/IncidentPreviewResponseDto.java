package com.backend.adapter.inbound.dto.response.incident;

import com.backend.adapter.inbound.dto.media.MediaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;

/**
 * Response DTO for previewing an incident with minimal details.
 *
 * @param title short title of the incident
 * @param media related media (images, videos, etc.)
 */
@Schema(description = "Preview information for an incident containing basic details for list views")
@Builder(toBuilder = true)
public record IncidentPreviewResponseDto(

    long id,

    @Schema(
        description = "Short descriptive title of the incident",
        example = "Road closure due to construction"
    )
    String title,

    @Schema(
        description = "Collection of related media files (images, videos, etc.)"
    )
    Set<MediaDto> media,

    @Schema(
        description = "Latitude coordinate where the incident is located",
        example = "52.5200"
    )
    double lat,

    @Schema(
        description = "Longitude coordinate where the incident is located",
        example = "13.4050"
    )
    double lon,

    @Schema(
        description = "Describes when the incident was created",
        example = "created at: 12:10PM"
    )
    Instant createdAt) { }
