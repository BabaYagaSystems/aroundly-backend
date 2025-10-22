package com.backend.adapter.outbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_REACTION_CLIENT"))
    private ClientEntity client;

    private Integer likes;
    private Integer dislikes;
}
