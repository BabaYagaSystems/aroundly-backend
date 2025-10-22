package com.backend.domain.mixins;

import com.backend.domain.reactions.SentimentEngagement;

/**
 * Mixin interface for objects that can be reacted to.
 */
public interface Reactable {

  /**
   * Returns the sentiment engagement associated with the object.
   *
   * @return the sentiment engagement
   */
  SentimentEngagement getSentimentEngagement();
}
