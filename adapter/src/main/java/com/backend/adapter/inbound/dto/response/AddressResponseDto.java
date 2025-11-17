package com.backend.adapter.inbound.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Simple DTO returned by REST endpoints when converting coordinates into a
 * human-readable address string.
 */
@Schema(description = "Response containing the address of requested coordinates")
public record AddressResponseDto(
    @Schema(
        description = "Human-readable formatted address",
        example = "str. Mamei Tale 69/1, Hârtopul Vechi, Moldova"
    )
    String address) { }
