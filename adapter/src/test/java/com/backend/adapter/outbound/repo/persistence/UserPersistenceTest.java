package com.backend.adapter.outbound.repo.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.repo.UserPersistenceRepository;
import com.backend.domain.actor.Role;
import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPersistenceTest {

  @Mock
  private UserPersistenceRepository repository;

  @InjectMocks
  private UserPersistence userPersistence;

  private User domainUser;
  private UserEntity entityUser;

  @BeforeEach
  void setUp() {
    domainUser = User.builder()
        .uid(new UserId("firebase-1"))
        .email("new@example.com")
        .name("New Name")
        .picture("new.png")
        .role(Role.USER)
        .deviceIdToken("token")
        .range(10)
        .build();

    entityUser = UserEntity.builder()
        .id(1L)
        .firebaseUid("firebase-1")
        .email("existing@example.com")
        .displayName("Existing")
        .pictureUrl("existing.png")
        .fcmToken("old-token")
        .rangeKm(5)
        .role(Role.USER)
        .isActive(true)
        .createdAt(Instant.now())
        .lastLogin(Instant.now())
        .build();
  }

  @Test
  void saveCreatesNewUserWhenUidNotFound() {
    when(repository.findByFirebaseUid("firebase-1")).thenReturn(Optional.empty());
    when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> {
      UserEntity entity = invocation.getArgument(0);
      entity.setId(5L);
      return entity;
    });

    User saved = userPersistence.save(domainUser);

    assertThat(saved.uid().value()).isEqualTo("firebase-1");
    assertThat(saved.email()).isEqualTo("new@example.com");
    verify(repository).save(any(UserEntity.class));
  }

  @Test
  void saveMergesExistingUserWhenUidFound() {
    when(repository.findByFirebaseUid("firebase-1")).thenReturn(Optional.of(entityUser));
    when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User saved = userPersistence.save(domainUser);

    assertThat(saved.email()).isEqualTo("new@example.com");
    assertThat(saved.name()).isEqualTo("New Name");
    assertThat(saved.picture()).isEqualTo("new.png");
    verify(repository).save(entityUser);
  }

  @Test
  void findByFirebaseUidReturnsDomainUser() {
    when(repository.findByFirebaseUid("firebase-1")).thenReturn(Optional.of(entityUser));

    Optional<User> result = userPersistence.findByFirebaseUid("firebase-1");

    assertThat(result).isPresent();
    assertThat(result.get().uid().value()).isEqualTo("firebase-1");
  }

  @Test
  void findByEmailReturnsDomainUser() {
    when(repository.findByEmail("existing@example.com")).thenReturn(Optional.of(entityUser));

    Optional<User> result = userPersistence.findByEmail("existing@example.com");

    assertThat(result).isPresent();
    assertThat(result.get().name()).isEqualTo("Existing");
  }

  @Test
  void existsDelegatesToRepository() {
    when(repository.existsByFirebaseUid("firebase-1")).thenReturn(true);
    assertThat(userPersistence.existsByFirebaseUid("firebase-1")).isTrue();
    verify(repository).existsByFirebaseUid("firebase-1");
  }
}
