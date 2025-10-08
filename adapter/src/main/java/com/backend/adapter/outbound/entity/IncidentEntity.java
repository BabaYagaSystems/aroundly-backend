package com.backend.adapter.outbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "incident_id_seq")
    @SequenceGenerator(name = "incident_id_seq", sequenceName = "incident_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "happening_id", foreignKey = @ForeignKey(name = "FK_INCIDENT_HAPPENING"))
    private HappeningEntity happening;

    @Column(name = "time_posted", nullable = false)
    private LocalDateTime timePosted;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private double range;

    @Column(nullable = false)
    private int confirms;

    @Column(nullable = false)
    private int denies;
}
