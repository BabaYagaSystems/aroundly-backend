package com.backend.port.inbound;


import com.backend.domain.happening.Incident;
import com.backend.port.inbound.commands.CreateIncidentCommand;
import com.backend.port.inbound.commands.RadiusCommand;
import java.util.List;

/**
 * Defines use cases specific to {@link Incident} management,
 * extending the general {@link HappeningUseCase}.
 */
public interface IncidentUseCase extends HappeningUseCase {

    /**
     * Finds all incidents within a given geographic radius.
     *
     * @param radiusCommand the command containing center coordinates and radius in meters
     * @return the list of incidents within the given radius
     */
    List<Incident> findAllInGivenRange(RadiusCommand radiusCommand);

    /**
     * Creates a new incident.
     *
     * @param createIncidentCommand the command containing incident details
     * @return the created incident
     */
    Incident create(CreateIncidentCommand createIncidentCommand);

    /**
     * Updates an existing incident.
     *
     * @param incidentId               the identifier of the incident to update
     * @param newCreateIncidentCommand the command containing updated incident details
     * @return the updated incident
     */
    Incident update(long incidentId, CreateIncidentCommand newCreateIncidentCommand);

    /**
     * Extends the lifespan of an incident, if possible.
     *
     * Typically, this is triggered by user confirmation and will
     * increase the expiration time by a fixed duration,
     * but never beyond the maximum allowed lifespan.
     *
     * @param incidentId the identifier of the incident
     * @return the updated incident with extended lifespan
     */
    Incident confirm(long incidentId);

    /**
     * Registers a denial for the given incident.
     *
     * - Increments both the total and consecutive denial counters.
     * - May cause the incident to be marked for deletion if the
     *   maximum number of consecutive denies is reached.
     *
     * @param incidentId the unique identifier of the incident
     * @return the updated {@link Incident} after the denial has been applied
     */
    Incident deny(long incidentId);

    /**
     * Deletes the given incident if it has expired.
     *
     * - An incident is considered expired once its {@code expiresAt} timestamp
     *   is in the past relative to the system clock.
     * - If the incident has not expired, this method does nothing.
     *
     * @param incidentId the unique identifier of the incident to check and possibly delete
     */
    void deleteIfExpired(long incidentId);
}
