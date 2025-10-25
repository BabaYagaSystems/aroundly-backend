package com.backend.domain.actor;

import com.backend.domain.happening.HappeningId;
import com.backend.domain.mixins.Reactable;
import com.backend.domain.mixins.TimeStamped;
import com.backend.domain.reactions.SentimentEngagement;
import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;

/**
 * Represents a user-generated comment associated with a {@code Happening} (such as an Event or Incident).
 * Contains the author's username, the comment content, and the timestamp of creation.
 */
@Builder(toBuilder = true)
public record Comment(
    @NonNull String text,
    @NonNull HappeningId happeningId,
    @NonNull ActorId actorId,
    Instant createdAt,
    SentimentEngagement getSentimentEngagement) implements TimeStamped, Reactable {

    /**
     * Constructs a {@code Comment} instance with input validation.
     *
     *   @throws IllegalArgumentException if:
     *   @param text is empty or contains only whitespace< ; is shorter than 5 characters
     */
    public Comment {
        if (text.trim().isEmpty())
            throw new IllegalArgumentException("Text cannot be empty or only spaces.");
    }
}
