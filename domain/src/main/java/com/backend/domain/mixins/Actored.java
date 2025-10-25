package com.backend.domain.mixins;

import com.backend.domain.actor.ActorId;

/**
 * Mixin interface for objects that are associated with an actor.
 */
public interface Actored {

  /**
   * Returns the identifier of the associated actor.
   *
   * @return the actor identifier
   */
  ActorId getActorId();

  /**
   * Checks whether the object was authored by the given actor.
   *
   * @param candidate the actor identifier to check
   * @return {@code true} if the candidate matches the object's actor, otherwise {@code false}
   */
  default boolean authoredBy(ActorId candidate) {
    return candidate.equals(getActorId());
  }
}
