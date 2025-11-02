package com.backend.domain.actor;

import lombok.Builder;
import lombok.NonNull;

/**
 * Represents authenticated user information extracted from Firebase token.
 * This is a domain object that contains the essential user data.
 */
@Builder
public record FirebaseUserInfo(
        @NonNull String uid,           // Firebase User ID (unique identifier)
        String email,                   // User's email (can be null)
        String name,                    // User's display name (can be null)
        String picture,                 // User's profile picture URL (can be null)
        boolean emailVerified           // Whether email is verified
) {
}