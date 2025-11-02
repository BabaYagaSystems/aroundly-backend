package com.backend.adapter.outbound.repo.persistence;

import com.backend.adapter.outbound.entity.UserEntity;
import com.backend.adapter.outbound.repo.UserRepository;
import com.backend.domain.actor.Role;
import com.backend.domain.actor.FirebaseUserInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Service to sync Firebase users with the local database.
 * Creates or updates user records when they authenticate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;

    /**
     * Ensures a user exists in the database for the given Firebase user.
     * Creates a new user if they don't exist, updates last login if they do.
     *
     * @param firebaseUser the authenticated Firebase user
     * @return the user entity from the database
     */
    @Transactional
    public UserEntity syncUser(FirebaseUserInfo firebaseUser) {
        Optional<UserEntity> existingUser = userRepository.findByFirebaseUid(firebaseUser.uid());

        if (existingUser.isPresent()) {
            // Update existing user
            UserEntity user = existingUser.get();
            user.setLastLogin(Instant.now());

            // Optionally update email/name if changed in Firebase
            if (firebaseUser.email() != null && !firebaseUser.email().equals(user.getEmail())) {
                user.setEmail(firebaseUser.email());
                log.info("Updated email for user {}", firebaseUser.uid());
            }
            if (firebaseUser.name() != null && !firebaseUser.name().equals(user.getDisplayName())) {
                user.setDisplayName(firebaseUser.name());
                log.info("Updated display name for user {}", firebaseUser.uid());
            }

            return userRepository.save(user);
        } else {
            // Create new user
            UserEntity newUser = UserEntity.builder()
                    .firebaseUid(firebaseUser.uid())
                    .email(firebaseUser.email())
                    .displayName(firebaseUser.name())
                    .role(Role.USER)  // Default role
                    .rangeKm(5)       // Default range
                    .isActive(true)
                    .createdAt(Instant.now())
                    .lastLogin(Instant.now())
                    .build();

            UserEntity saved = userRepository.save(newUser);
            log.info("Created new user in database: {} ({})", saved.getFirebaseUid(), saved.getEmail());
            return saved;
        }
    }

    /**
     * Gets or creates a user for the currently authenticated Firebase user.
     *
     * @param firebaseUser the authenticated Firebase user
     * @return the user entity
     */
    public UserEntity getOrCreateUser(FirebaseUserInfo firebaseUser) {
        return syncUser(firebaseUser);
    }

    /**
     * Updates the FCM token for push notifications.
     *
     * @param firebaseUid the Firebase user ID
     * @param fcmToken the FCM token
     */
    @Transactional
    public void updateFcmToken(String firebaseUid, String fcmToken) {
        userRepository.findByFirebaseUid(firebaseUid).ifPresent(user -> {
            user.setFcmToken(fcmToken);
            userRepository.save(user);
            log.info("Updated FCM token for user {}", firebaseUid);
        });
    }

    /**
     * Updates the notification range for a user.
     *
     * @param firebaseUid the Firebase user ID
     * @param rangeKm the range in kilometers
     */
    @Transactional
    public void updateRange(String firebaseUid, Integer rangeKm) {
        userRepository.findByFirebaseUid(firebaseUid).ifPresent(user -> {
            user.setRangeKm(rangeKm);
            userRepository.save(user);
            log.info("Updated range to {}km for user {}", rangeKm, firebaseUid);
        });
    }
}