package com.backend.adapter.outbound.repo.redis;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.ReactionEntity;
import com.backend.adapter.outbound.repo.IncidentPersistenceRepository;
import com.backend.adapter.outbound.repo.ReactionPersistenceRepository;
import com.backend.port.inbound.commands.ReactionSummary;
import com.backend.port.inbound.commands.ReactionType;
import com.backend.port.outbound.repo.ReactionRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Redis-backed implementation of {@link ReactionRepository} that keeps incident reactions in Redis
 * for fast reads while mirroring the latest state into the relational persistence layer.
 */
@Repository
@RequiredArgsConstructor
public class IncidentReactionRepository implements ReactionRepository {

  private static final String REACTION_LUA = """
        local likeKey = KEYS[1]
        local dislikeKey = KEYS[2]
        local userId = ARGV[1]
        local action = ARGV[2]

        if action == "ADD_LIKE" then
            redis.call("SREM", dislikeKey, userId)
            redis.call("SADD", likeKey, userId)
        elseif action == "ADD_DISLIKE" then
            redis.call("SREM", likeKey, userId)
            redis.call("SADD", dislikeKey, userId)
        elseif action == "REMOVE_LIKE" then
            redis.call("SREM", likeKey, userId)
        elseif action == "REMOVE_DISLIKE" then
            redis.call("SREM", dislikeKey, userId)
        elseif action == "CLEAR" or action == "REFRESH" then
            if action == "CLEAR" then
                redis.call("SREM", likeKey, userId)
                redis.call("SREM", dislikeKey, userId)
            end
        else
            return redis.error_reply("Unknown reaction action " .. action)
        end

        local likes = redis.call("SCARD", likeKey)
        local dislikes = redis.call("SCARD", dislikeKey)
        local reactionFlag = 0

        if redis.call("SISMEMBER", likeKey, userId) == 1 then
            reactionFlag = 1
        elseif redis.call("SISMEMBER", dislikeKey, userId) == 1 then
            reactionFlag = -1
        end

        return {likes, dislikes, reactionFlag}
        """;

//  private static final DefaultRedisScript<List> REACTION_SCRIPT;
//
//  static {
//    REACTION_SCRIPT = new DefaultRedisScript<>();
//    REACTION_SCRIPT.setScriptText(REACTION_LUA);
//    REACTION_SCRIPT.setResultType(List.class);
//  }
//
//  private final IncidentPersistenceRepository incidentRepository;
//  private final ReactionPersistenceRepository reactionPersistenceRepository;
//  private final StringRedisTemplate redisTemplate;
//  private final ReactionKeyBuilder keyBuilder;

  /**
   * Applies a like reaction by toggling the Redis sets and persisting the user’s latest choice.
   */
  @Override
  @Transactional
  public ReactionSummary addLike(long incidentId, String userId) {
    return null;
//    ReactionSummary summary = execute(incidentId, userId, "ADD_LIKE");
//    IncidentEntity incidentEntity = incidentRepository.getReferenceById(incidentId);
//
//    reactionPersistenceRepository.findByIncidentId(incidentId)
//        .ifPresentOrElse(existing -> {
//          if (existing.getReactionType() != ReactionType.LIKE) {
//            existing.setReactionType(ReactionType.LIKE);
//            existing.setReactedAt(Instant.now());
//          }
//        }, () -> reactionPersistenceRepository.save(
//            ReactionEntity.builder()
//                .incident(incidentEntity)
//                .reactionType(ReactionType.LIKE)
//                .reactedAt(Instant.now())
//                .build()
//        ));
//
//    return summary;
  }

  /**
   * Applies a dislike reaction by toggling the Redis sets and persisting the user’s latest choice.
   */
  @Override
  @Transactional
  public ReactionSummary addDislike(long incidentId, String userId) {
//    ReactionSummary summary = execute(incidentId, userId, "ADD_DISLIKE");
//    IncidentEntity incident = incidentRepository.getReferenceById(incidentId);
//
//    reactionPersistenceRepository.findByIncidentId(incidentId)
//        .ifPresentOrElse(existing -> {
//          if (existing.getReactionType() != ReactionType.DISLIKE) {
//            existing.setReactionType(ReactionType.DISLIKE);
//            existing.setReactedAt(Instant.now());
//          }
//        }, () -> reactionPersistenceRepository.save(
//            ReactionEntity.builder()
//                .incident(incident)
//                .reactionType(ReactionType.DISLIKE)
//                .reactedAt(Instant.now())
//                .build()
//        ));

//    return summary;
    return null;
  }

  /**
   * Removes a like reaction, clearing both Redis and the durable record.
   */
  @Override
  @Transactional
  public ReactionSummary removeLike(long incidentId, String userId) {
//    ReactionSummary summary = execute(incidentId, userId, "REMOVE_LIKE");
//
////    ClientEntity client = clientRepository.findByExternalId(userId)
////        .orElseThrow(() -> new IllegalArgumentException("Unknown client " + userId));
//
//    reactionPersistenceRepository.findByIncidentId(incidentId)
//        .ifPresent(existing -> {
//          if (existing.getReactionType() == ReactionType.LIKE) {
//            reactionPersistenceRepository.delete(existing);
//          }
//        });
//
//    return summary;
    return null;
  }

  /**
   * Removes a dislike reaction, clearing both Redis and the durable record.
   */
  @Override
  @Transactional
  public ReactionSummary removeDislike(long incidentId, String userId) {
//    ReactionSummary summary = execute(incidentId, userId, "REMOVE_DISLIKE");
//
////    ClientEntity client = clientRepository.findByExternalId(userId)
////        .orElseThrow(() -> new IllegalArgumentException("Unknown client " + userId));
//
//    reactionPersistenceRepository.findByIncidentId(incidentId)
//        .ifPresent(existing -> {
//          if (existing.getReactionType() == ReactionType.DISLIKE) {
//            reactionPersistenceRepository.delete(existing);
//          }
//        });
//
//    return summary;
    return null;
  }

  /**
   * Fetches current like/dislike counters from Redis without mutating state.
   */
  @Override
  @Transactional(readOnly = true)
  public ReactionSummary getSummary(long incidentId, String userId) {
    return execute(incidentId, userId, "REFRESH");
  }

  /**
   * Executes the Lua script that coordinates reaction mutations and returns updated counters.
   */
  private ReactionSummary execute(long incidentId, String userId, String action) {
//    List<String> keys = List.of(
//        keyBuilder.likesKey(incidentId),
//        keyBuilder.dislikesKey(incidentId)
//    );
//
//    @SuppressWarnings("unchecked")
//    List<Long> raw =
//        (List<Long>) redisTemplate.execute(REACTION_SCRIPT, keys, userId, action);
//
//    if (raw.size() != 3) {
//      throw new IllegalStateException("Unexpected Redis response for incident " + incidentId);
//    }
//
//    ReactionType reactionType = ReactionType.fromToken(raw.get(2));
//    return new ReactionSummary(
//        incidentId,
//        raw.get(0).intValue(),
//        raw.get(1).intValue(),
//        reactionType
//    );
    return null;
  }
}
