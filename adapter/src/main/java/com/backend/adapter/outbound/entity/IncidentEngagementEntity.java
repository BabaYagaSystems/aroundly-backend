package com.backend.adapter.outbound.entity;

import com.backend.domain.reactions.IncidentEngagementType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "incident_engagements")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEngagementEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "incident_engagement_id_seq")
  @SequenceGenerator(name = "incident_engagement_id_seq", sequenceName = "incident_engagement_id_seq", allocationSize = 1)
  private long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "incident_id", foreignKey = @ForeignKey(name = "FK_INCIDENT_ENGAGEMENT_INCIDENT"))
  private IncidentEntity incident;

  private String userId;

  @Enumerated(EnumType.STRING)
  private IncidentEngagementType engagementType;

  private Instant engagedAt;
}
