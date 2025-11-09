package com.backend.adapter.outbound.entity;

import com.backend.port.inbound.commands.ReactionType;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity storing the latest reaction a client expressed toward either an incident or comment.
 * Acts as the durable backing for the Redis reaction cache.
 */
@Entity(name = "reactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reaction_id_seq")
    @SequenceGenerator(name = "reaction_id_seq", sequenceName = "reaction_id_seq", allocationSize = 1)
    private long id;

    @ManyToOne
    @JoinColumn(name = "incident_id", foreignKey = @ForeignKey(name = "FK_REACTION_INCIDENT"))
    private IncidentEntity incident;

    @ManyToOne
    @JoinColumn(name = "comment_id", foreignKey = @ForeignKey(name = "FK_REACTION_COMMENT"))
    private CommentEntity comment;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_REACTION_USER"))
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    private Instant reactedAt;
}
