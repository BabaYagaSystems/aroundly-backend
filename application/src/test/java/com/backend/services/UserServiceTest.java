package com.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.domain.actor.Role;
import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import com.backend.port.inbound.commands.UpdateDeviceIdTokenCommand;
import com.backend.port.inbound.commands.UpdateRangeCommand;
import com.backend.port.outbound.repo.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void createPersistsUserWhenMissing() {
    final User newUser =
        buildUser("firebase-123", "new@example.com", "New User", "picture", Role.USER, null, 10);

    when(userRepository.existsByFirebaseUid("firebase-123")).thenReturn(false);
    when(userRepository.save(newUser)).thenReturn(newUser);

    final Optional<User> stored = userService.create(newUser);

    assertThat(stored).contains(newUser);
    verify(userRepository).save(newUser);
  }

  @Test
  void createMergesExistingUserWhenAlreadyPersisted() {
    final User incoming =
        buildUser("firebase-123", "fresh@example.com", "Fresh Name", "fresh-picture", Role.USER, null, 10);
    final User persisted =
        buildUser("firebase-123", "old@example.com", "Old Name", "old-picture", Role.USER, null, 10);

    when(userRepository.existsByFirebaseUid("firebase-123")).thenReturn(true);
    when(userRepository.findByFirebaseUid("firebase-123")).thenReturn(Optional.of(persisted));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final Optional<User> stored = userService.create(incoming);

    assertThat(stored).isPresent();
    final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    User merged = captor.getValue();
    assertThat(merged.email()).isEqualTo("fresh@example.com");
    assertThat(merged.name()).isEqualTo("Fresh Name");
    assertThat(merged.picture()).isEqualTo("fresh-picture");
  }

  @Test
  void updateDeviceIdTokenPersistsNewToken() {
    final User existing = buildUser("firebase-123", "user@example.com", "User", "pic", Role.USER, null, 5);
    when(userRepository.findByFirebaseUid("firebase-123")).thenReturn(Optional.of(existing));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final Optional<User> updated =
        userService.updateDeviceIdToken(
            new UpdateDeviceIdTokenCommand("firebase-123", "new-token"));

    assertThat(updated).isPresent();
    final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().deviceIdToken()).isEqualTo("new-token");
  }

  @Test
  void updateDeviceIdTokenDoesNothingWhenUserMissing() {
    when(userRepository.findByFirebaseUid("missing")).thenReturn(Optional.empty());

    final Optional<User> result =
        userService.updateDeviceIdToken(new UpdateDeviceIdTokenCommand("missing", "token"));

    assertThat(result).isEmpty();
    verify(userRepository, never()).save(any());
  }

  @Test
  void updateRangePersistsNewRange() {
    final User existing = buildUser("firebase-123", "user@example.com", "User", "pic", Role.USER, null, 5);
    when(userRepository.findByFirebaseUid("firebase-123")).thenReturn(Optional.of(existing));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final Optional<User> updated =
        userService.updateRange(new UpdateRangeCommand("firebase-123", 15));

    assertThat(updated).isPresent();
    final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().range()).isEqualTo(15);
  }

  private static User buildUser(
      String firebaseUid,
      String email,
      String name,
      String picture,
      Role role,
      String deviceIdToken,
      int range) {
    return User.builder()
        .uid(new UserId(firebaseUid))
        .email(email)
        .name(name)
        .picture(picture)
        .role(role)
        .deviceIdToken(deviceIdToken)
        .range(range)
        .build();
  }
}
