package com.backend.adapter.inbound.dto.response.incident;

import lombok.Builder;
import java.util.List;

@Builder
public record IncidentWithBytesDto(
        String title,
        List<MediaBytes> media,
        double lat,
        double lon
) {
    @Builder
    public record MediaBytes(
            String filename,
            String contentType,
            long size,
            String data
    ) {}
}