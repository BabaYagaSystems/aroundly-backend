package com.backend.domain.mixins;

import com.backend.domain.location.LocationId;

/**
 * Mixin interface for objects that can be associated with a location.
 */
public interface Locatable {

  /**
   * Returns the identifier of the associated location.
   *
   * @return the location identifier
   */
  LocationId getLocationId();
}
