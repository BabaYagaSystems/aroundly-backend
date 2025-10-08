package com.backend.adapter.outbound.entity;

import static jakarta.persistence.CascadeType.ALL;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "happenings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HappeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "happening_id_seq")
    @SequenceGenerator(name = "happening_id_seq", sequenceName = "happening_id_seq", allocationSize = 1)
    private Long id;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_HAPPENING_CLIENT"))
    private ClientEntity client;

    @ManyToOne
    @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "FK_HAPPENING_LOCATION"))
    private LocationEntity location;

    @OneToMany(mappedBy = "happeningEntity", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MediaEntity> media = new HashSet<>();

    private LocalDateTime createdAt;

    // --- Explicit getters and setters for MapStruct ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setClient(ClientEntity client) {
        this.client = client;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void addMedia(MediaEntity m) {
        media.add(m);
        m.setHappeningEntity(this);
    }

    public void removeMedia(MediaEntity m) {
        media.remove(m);
        m.setHappeningEntity(null);
    }
}
