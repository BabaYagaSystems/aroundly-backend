package com.backend.adapter.in.dto.response.incident;

import com.backend.domain.media.Media;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(
        description = "Short descriptive title of the incident",
        example = "Road closure due to construction"
    )
    String title,

    @Schema(
        description = "Collection of related media files (images, videos, etc.)"
    )
    Set<Media> media) { }
