package com.backend.port.inbound;

import com.backend.domain.happening.Happening;
import java.util.List;

/**
 * Defines use cases for managing {@link Happening} objects,
 * including retrieval and deletion.
 */
public interface HappeningUseCase {

  /**
   * Finds a Happening by its unique identifier.
   *
   * @param happeningId the identifier of the Happening
   * @return the Happening with the given id
   */
  Happening findById(long happeningId);

  /**
   * Deletes a Happening by its unique identifier.
   *
   * @param happeningId the identifier of the Happening to delete
   */
  void deleteById(long happeningId);

  /**
   * Finds all Happenings authored by a given actor.
   *
   * @param actorId the identifier of the actor
   * @return the list of Happenings by the actor
   */
  List<Happening> findByActorId(String actorId);
}
