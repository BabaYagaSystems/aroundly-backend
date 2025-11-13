package com.backend.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.domain.actor.Role;
import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import com.backend.port.inbound.commands.UpdateDeviceIdTokenCommand;
import com.backend.port.inbound.commands.UpdateRangeCommand;
import com.backend.port.outbound.repo.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

  private InMemoryUserRepository userRepository;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = new InMemoryUserRepository();
    userService = new UserService(userRepository);
  }

  @Test
  void createPersistsUserWhenMissing() {
    User newUser =
        buildUser("firebase-123", "new@example.com", "New User", "picture", Role.USER, null, 10);

    Optional<User> stored = userService.create(newUser);

    assertThat(stored).contains(newUser);
    assertThat(userRepository.findByFirebaseUid("firebase-123")).contains(newUser);
  }

  @Test
  void createMergesExistingUserWhenAlreadyPersisted() {
    userRepository.save(
        buildUser("firebase-123", "old@example.com", "Old Name", "old-picture", Role.USER, null, 10));
    User incoming =
        buildUser("firebase-123", "fresh@example.com", "Fresh Name", "fresh-picture", Role.USER, null, 10);

    Optional<User> stored = userService.create(incoming);

    assertThat(stored).isPresent();
    User merged = userRepository.findByFirebaseUid("firebase-123").orElseThrow();
    assertThat(merged.email()).isEqualTo("fresh@example.com");
    assertThat(merged.name()).isEqualTo("Fresh Name");
    assertThat(merged.picture()).isEqualTo("fresh-picture");
  }

  @Test
  void updateDeviceIdTokenPersistsNewToken() {
    userRepository.save(
        buildUser("firebase-123", "user@example.com", "User", "pic", Role.USER, null, 5));

    Optional<User> updated =
        userService.updateDeviceIdToken(
            new UpdateDeviceIdTokenCommand("firebase-123", "new-token"));

    assertThat(updated).isPresent();
    assertThat(userRepository.findByFirebaseUid("firebase-123").orElseThrow().deviceIdToken())
        .isEqualTo("new-token");
  }

  @Test
  void updateDeviceIdTokenDoesNothingWhenUserMissing() {
    Optional<User> result =
        userService.updateDeviceIdToken(new UpdateDeviceIdTokenCommand("missing", "token"));

    assertThat(result).isEmpty();
    assertThat(userRepository.data).isEmpty();
  }

  @Test
  void updateRangePersistsNewRange() {
    userRepository.save(
        buildUser("firebase-123", "user@example.com", "User", "pic", Role.USER, null, 5));

    Optional<User> updated =
        userService.updateRange(new UpdateRangeCommand("firebase-123", 15));

    assertThat(updated).isPresent();
    assertThat(userRepository.findByFirebaseUid("firebase-123").orElseThrow().range())
        .isEqualTo(15);
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

  private static class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> data = new HashMap<>();

    @Override
    public User save(User user) {
      data.put(user.uid().value(), user);
      return user;
    }

    @Override
    public Optional<User> findByFirebaseUid(String firebaseUid) {
      return Optional.ofNullable(data.get(firebaseUid));
    }

    @Override
    public Optional<User> findByEmail(String email) {
      return data.values().stream().filter(user -> email.equals(user.email())).findFirst();
    }

    @Override
    public boolean existsByFirebaseUid(String firebaseUid) {
      return data.containsKey(firebaseUid);
    }
  }
}
