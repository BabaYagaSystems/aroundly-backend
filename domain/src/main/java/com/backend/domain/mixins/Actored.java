package com.backend.domain.mixins;

/**
 * Mixin interface for objects that are associated with an actor.
 */
public interface Actored {

  /**
   * Returns the identifier of the associated actor.
   *
   * @return the actor identifier
   */
  long getActorId();
}
