package com.backend.adapter.outbound.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import java.time.OffsetDateTime;
import lombok.Builder;

/**
 * Entity representing a stored media object.
 *
 * This entity contains metadata about uploaded media, including
 * its storage key, content type, size, and creation timestamp.
 */
@Entity(name = "medias")
@Builder
public class MediaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "media_id_seq")
  @SequenceGenerator(name = "media_id_seq", sequenceName = "media_id_seq", allocationSize = 1)
  private long id;

  @Column(name = "key", nullable = false, unique = true)
  private String key;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "size", nullable = false)
  private long size;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = LAZY, optional = false)
  @JoinColumn(name = "incident_id", foreignKey = @ForeignKey(name = "FK_MEDIA_INCIDENT"))
  private IncidentEntity incidentEntity;

  public MediaEntity(long id, String key, String contentType, long size, OffsetDateTime createdAt,
      IncidentEntity incidentEntity) {
    this.id = id;
    this.key = key;
    this.contentType = contentType;
    this.size = size;
    this.createdAt = createdAt;
    this.incidentEntity = incidentEntity;
  }

  public MediaEntity() {
  }

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public IncidentEntity getIncidentEntity() {
    return incidentEntity;
  }

  public void setIncidentEntity(IncidentEntity incidentEntity) {
    this.incidentEntity = incidentEntity;
  }
}
