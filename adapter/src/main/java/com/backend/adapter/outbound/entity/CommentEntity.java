package com.backend.adapter.outbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_id_seq")
    @SequenceGenerator(name = "comment_id_seq", sequenceName = "comment_id_seq", allocationSize = 1)
    private long id;

    @ManyToOne
    @JoinColumn(name = "incident_id", foreignKey = @ForeignKey(name = "FK_COMMENT_INCIDENT"))
    private IncidentEntity incident;

    @ManyToOne
    @JoinColumn(
            name = "user_uid",
            referencedColumnName = "firebase_uid",
            foreignKey = @ForeignKey(name = "FK_COMMENT_USER"))
    private UserEntity user;

    @Column(columnDefinition = "TEXT")
    private String value;

    private LocalDateTime createdAt;
}
