package com.backend.services;

import com.backend.domain.actor.FirebaseUserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service responsible for validating Firebase tokens and extracting user information.
 * Uses Firebase Admin SDK to verify token authenticity.
 */
@Service
@Slf4j
public class FirebaseTokenValidator {

    /**
     * Validates a Firebase ID token and extracts user information.
     *
     * @param idToken the Firebase ID token from the Authorization header
     * @return Optional containing FirebaseUserInfo if token is valid, empty otherwise
     */
    public Optional<FirebaseUserInfo> validateToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            FirebaseUserInfo userInfo = FirebaseUserInfo.builder()
                    .uid(decodedToken.getUid())
                    .email(decodedToken.getEmail())
                    .name(decodedToken.getName())
                    .picture(decodedToken.getPicture())
                    .emailVerified(decodedToken.isEmailVerified())
                    .build();

            log.debug("Successfully validated token for user: {}", userInfo.uid());
            return Optional.of(userInfo);

        } catch (FirebaseAuthException e) {
            log.warn("Invalid Firebase token: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error validating Firebase token", e);
            return Optional.empty();
        }
    }

    /**
     * Extracts the token from the Authorization header.
     * Expected format: "Bearer <token>"
     *
     * @param authHeader the Authorization header value
     * @return Optional containing the token if format is valid, empty otherwise
     */
    public Optional<String> extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }
}
