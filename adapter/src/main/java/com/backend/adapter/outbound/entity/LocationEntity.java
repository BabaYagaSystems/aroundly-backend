package com.backend.adapter.outbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "locations")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_id_seq")
    @SequenceGenerator(name = "location_id_seq", sequenceName = "location_id_seq", allocationSize = 1)
    private long id;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lng", nullable = false)
    private double lng;

    @Column(name = "address_text")
    private String addressText;
}