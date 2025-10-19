package com.backend.port.outbound.repo;

import com.backend.domain.happening.Happening;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing persistence of {@link Happening} objects.
 *
 * Provides methods for retrieving and deleting happenings.
 */
public interface HappeningRepository {

  /**
   * Finds a Happening by its unique identifier.
   *
   * @param happeningId the identifier of the Happening
   * @return the Happening with the given id
   */
  Optional<Happening> findById(long happeningId);

  /**
   * Checks if a Happening exists by its unique identifier.
   *
   * @param happeningId the identifier of the Happening
   * @return the Happening with the given id
   */
  boolean existsById(long happeningId);

  /**
   * Finds all Happenings authored by a given user.
   *
   * @param userId the identifier of the user (actor)
   * @return the list of Happenings created by the user
   */
  List<Happening> findByUserId(String userId);

  /**
   * Deletes a Happening by its unique identifier.
   *
   * @param happeningId the identifier of the Happening to delete
   */
  void deleteById(long happeningId);
}
