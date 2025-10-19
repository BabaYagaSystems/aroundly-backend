package com.backend.port.outbound.repo;

import com.backend.domain.happening.Incident;
import java.util.List;

/**
 * Repository interface for managing incidents in the data store.
 */
public interface IncidentRepository extends HappeningRepository {

    /**
     * Saves a new incident or updates an existing one.
     *
     * @param incident The incident to be saved or updated.
     * @return The saved or updated incident (with assigned ID if new).
     */
    Incident save(Incident incident);

    /**
     * Finds all incidents within a specified range (e.g., geographical or time-based).
     * Consider adding parameters such as location coordinates and range value.
     *
     * @return A list of incidents within the given range.
     */
    List<Incident> findAllInGivenRange(double lat, double lon, double radiusMeters);
}
