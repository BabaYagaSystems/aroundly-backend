package com.backend.port.inbound.commands;

/**
 * Represents the possible sentiment reactions an actor can express toward a reactable entity.
 */
public enum ReactionType {
  LIKE,
  DISLIKE,
  NONE;

  /**
   * Convenience helper that turns numeric flags (e.g. Redis Lua script output) into enum values.
   *
   * @param token positive for like, negative for dislike, zero for none
   * @return matching reaction type
   */
  public static ReactionType fromToken(long token) {
    if (token > 0) {
      return LIKE;
    }
    if (token < 0) {
      return DISLIKE;
    }
    return NONE;
  }
}
