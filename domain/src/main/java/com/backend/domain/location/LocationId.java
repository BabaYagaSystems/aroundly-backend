package com.backend.domain.location;

/**
 * Represents a unique identifier for a location.
 *
 * @param value the identifier value (can be null before persistence)
 */
public record LocationId(Long value) { }