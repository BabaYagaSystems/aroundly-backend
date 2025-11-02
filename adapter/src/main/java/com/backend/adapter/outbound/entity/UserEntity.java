package com.backend.adapter.outbound.entity;

import com.backend.domain.actor.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing a user in the system.
 * The firebaseUid is the unique identifier from Firebase Authentication,
 * while the database ID is used for internal relationships.
 */
@Entity(name = "users")
@Table(name = "users", indexes = {
        @Index(name = "idx_firebase_uid", columnList = "firebase_uid", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;

    /**
     * Firebase User ID - the unique identifier from Firebase Authentication.
     * This links our database user to the Firebase user.
     */
    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    /**
     * User's email address (from Firebase).
     */
    @Column(name = "email", length = 255)
    private String email;

    /**
     * User's display name (from Firebase).
     */
    @Column(name = "display_name", length = 255)
    private String displayName;

    /**
     * User's profile picture URL (from Firebase).
     */
    @Column(name = "picture_url", length = 512)
    private String pictureUrl;

    /**
     * FCM (Firebase Cloud Messaging) token for push notifications.
     */
    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    /**
     * Notification range in kilometers.
     * Users receive notifications for incidents within this range.
     */
    @Column(name = "range_km")
    private Integer rangeKm;

    /**
     * User role in the system.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /**
     * Whether the user account is active.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * When the user was created in our system.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Last time the user logged in.
     */
    @Column(name = "last_login")
    private Instant lastLogin;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (role == null) {
            role = Role.USER;
        }
        if (rangeKm == null) {
            rangeKm = 5; // Default 5km range
        }
    }
}