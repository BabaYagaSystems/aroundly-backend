package com.backend.adapter.outbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_id_seq")
    @SequenceGenerator(name = "client_id_seq", sequenceName = "client_id_seq", allocationSize = 1)
    private long id;

    @Column(name = "foreign_id", nullable = false)
    private String foreignId;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "range_km")
    private Integer range;
}