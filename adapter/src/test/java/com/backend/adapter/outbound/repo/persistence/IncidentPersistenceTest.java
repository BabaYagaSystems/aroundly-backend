package com.backend.adapter.outbound.repo.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.adapter.outbound.entity.IncidentEntity;
import com.backend.adapter.outbound.entity.LocationEntity;
import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.mapper.IncidentMapper;
import com.backend.adapter.outbound.repo.IncidentPersistenceRepository;
import com.backend.adapter.outbound.repo.LocationPersistenceRepository;
import com.backend.adapter.outbound.repo.UserPersistenceRepository;
import com.backend.domain.actor.Role;
import com.backend.domain.actor.UserId;
import com.backend.domain.happening.Incident;
import com.backend.domain.happening.IncidentId;
import com.backend.domain.location.LocationId;
import com.backend.domain.media.Media;
import com.backend.domain.reactions.EngagementStats;
import com.backend.domain.reactions.SentimentEngagement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentPersistenceTest {

  @Mock private IncidentPersistenceRepository incidentRepository;
  @Mock private LocationPersistenceRepository locationRepository;
  @Mock private UserPersistenceRepository userRepository;

  private IncidentMapper incidentMapper;
  private IncidentPersistence incidentPersistence;

  private Incident domainIncident;
  private IncidentEntity entityIncident;
  private LocationEntity locationEntity;
  private UserEntity userEntity;

  @BeforeEach
  void setUp() {
    incidentMapper = new IncidentMapper(locationRepository, userRepository);
    incidentPersistence = new IncidentPersistence(incidentRepository, incidentMapper);

    locationEntity = LocationEntity.builder()
        .id(10L)
        .lat(10.0)
        .lng(20.0)
        .addressText("Main St")
        .build();

    userEntity = UserEntity.builder()
        .id(5L)
        .firebaseUid("firebase-1")
        .email("user@example.com")
        .displayName("User")
        .role(Role.USER)
        .isActive(true)
        .createdAt(Instant.now())
        .build();

    domainIncident = Incident.builder()
        .id(new IncidentId(1L))
        .userId(new UserId("firebase-1"))
        .locationId(new LocationId(10L))
        .title("Road issue")
        .description("desc")
        .media(Set.of(new Media(1, "photo.png", "image/png")))
        .sentimentEngagement(SentimentEngagement.builder().likes(0).dislikes(0).build())
        .engagementStats(new EngagementStats(0, 0, 0))
        .expiresAt(Instant.now())
        .build();

    entityIncident = IncidentEntity.builder()
        .id(1L)
        .title("Road issue")
        .description("desc")
        .location(locationEntity)
        .user(userEntity)
        .media(Set.of())
        .confirms(0)
        .denies(0)
        .expiresAt(Instant.now())
        .build();
  }

  @Test
  void savePersistsAndReturnsMappedDomain() {
    when(locationRepository.findById(10L)).thenReturn(Optional.of(locationEntity));
    when(userRepository.findByFirebaseUid("firebase-1")).thenReturn(Optional.of(userEntity));
    when(incidentRepository.save(any(IncidentEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Incident saved = incidentPersistence.save(domainIncident);

    assertThat(saved.getTitle()).isEqualTo("Road issue");
    verify(incidentRepository).save(any(IncidentEntity.class));
  }

  @Test
  void findByIdReturnsMappedIncident() {
    when(incidentRepository.findById(1L)).thenReturn(Optional.of(entityIncident));

    Optional<Incident> result = incidentPersistence.findById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getId().value()).isEqualTo(1L);
  }

  @Test
  void findByIdThrowsWhenMissing() {
    when(incidentRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class, () -> incidentPersistence.findById(2L));
  }

  @Test
  void existsDelegatesToRepository() {
    when(incidentRepository.existsById(5L)).thenReturn(true);

    assertThat(incidentPersistence.existsById(5L)).isTrue();
    verify(incidentRepository).existsById(5L);
  }

  @Test
  void findAllInGivenRangeMapsEntities() {
    IncidentEntity entity2 = incidentEntity(2L);
    when(incidentRepository.findAllInGivenRange(1.0, 2.0, 500))
        .thenReturn(List.of(entityIncident, entity2));

    List<Incident> incidents = incidentPersistence.findAllInGivenRange(1.0, 2.0, 500);

    assertThat(incidents).hasSize(2);
    assertThat(incidents.get(0).getId().value()).isEqualTo(1L);
    assertThat(incidents.get(1).getId().value()).isEqualTo(2L);
  }

  @Test
  void deleteDelegatesToRepository() {
    incidentPersistence.deleteById(9L);
    verify(incidentRepository).deleteById(9L);
  }

  @Test
  void findByUserIdMapsEntities() {
    IncidentEntity entity2 = incidentEntity(3L);
    when(incidentRepository.findByUserFirebaseUid("firebase-1"))
        .thenReturn(List.of(entityIncident, entity2));

    List<Incident> incidents = incidentPersistence.findByUserId("firebase-1");

    assertThat(incidents).hasSize(2);
    assertThat(incidents.get(0).getUserId().value()).isEqualTo("firebase-1");
  }

  private IncidentEntity incidentEntity(long id) {
    return IncidentEntity.builder()
        .id(id)
        .title("Road issue")
        .description("desc")
        .location(locationEntity)
        .user(userEntity)
        .media(Set.of())
        .confirms(0)
        .denies(0)
        .expiresAt(Instant.now())
        .build();
  }
}
