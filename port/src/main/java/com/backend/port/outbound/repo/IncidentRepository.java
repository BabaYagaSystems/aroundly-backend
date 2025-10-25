package com.backend.port.outbound.repo;

import com.backend.domain.happening.Incident;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing incidents in the data store.
 */
public interface IncidentRepository {

  /**
   * Finds a Happening by its unique identifier.
   *
   * @param incidentId the identifier of the Happening
   * @return the Happening with the given id
   */
  Optional<Incident> findById(long incidentId);

  /**
   * Checks if an Incident exists by its unique identifier.
   *
   * @param incidentId the identifier of the Happening
   * @return the Happening with the given id
   */
  boolean existsById(long incidentId);

  /**
   * Finds all Happenings authored by a given user.
   *
   * @param userId the identifier of the user (actor)
   * @return the list of Happenings created by the user
   */
  List<Incident> findByUserId(String userId);

  /**
   * Deletes a Happening by its unique identifier.
   *
   * @param incidentId the identifier of the Happening to delete
   */
  void deleteById(long incidentId);

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
