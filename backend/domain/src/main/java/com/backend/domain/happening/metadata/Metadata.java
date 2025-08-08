package com.backend.domain.happening.metadata;

import com.backend.domain.happening.Event;
import com.backend.domain.happening.Happening;
import com.backend.domain.happening.Incident;

/**
 * Mixin interface for metadata associated with {@link Happening} entities,
 * such as {@link Incident} or {@link Event}.
 *
 * This interface is intended to be implemented by types like {@code EventMetadata}
 * or {@code IncidentMetadata} to provide contextual information such as time,
 * location, or reporting details.
 *
 * Serves as a structural marker for polymorphic behavior without enforcing method contracts.
 */
public interface Metadata { }
