package com.backend.adapter.outbound.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity(name = "incidents")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "incident_id_seq")
  @SequenceGenerator(name = "incident_id_seq", sequenceName = "incident_id_seq", allocationSize = 1)
  private long id;

  private String title;
  private String description;

  @ManyToOne
  @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "FK_INCIDENT_LOCATION"))
  private LocationEntity location;

  @OneToMany(mappedBy = "incidentEntity", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<MediaEntity> media = new HashSet<>();

  @ManyToOne
  @JoinColumn(
      name = "user_uid",
      referencedColumnName = "firebase_uid",
      foreignKey = @ForeignKey(name = "FK_INCIDENT_USER"))
  private UserEntity user;

  private Instant timePosted;
  private double range;
  private int confirms;
  private int denies;
  private int consecutiveDenies;
  private Instant expiresAt;

  public void addMedia(MediaEntity m) {
    media.add(m);
    m.setIncidentEntity(this);
  }

  public void removeMedia(MediaEntity m) {
    media.remove(m);
    m.setIncidentEntity(null);
  }

}
