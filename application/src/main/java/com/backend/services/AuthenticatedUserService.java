package com.backend.services;

import com.backend.domain.actor.FirebaseUserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service to access currently authenticated user information.
 * This service can be injected into your use cases when you need to know who's making the request.
 */
@Service
public class AuthenticatedUserService {

    /**
     * Gets the currently authenticated user from the security context.
     *
     * @return Optional containing FirebaseUserInfo if user is authenticated, empty otherwise
     */
    public Optional<FirebaseUserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof FirebaseUserInfo userInfo) {
            return Optional.of(userInfo);
        }

        return Optional.empty();
    }

    /**
     * Gets the UID of the currently authenticated user.
     *
     * @return Optional containing the user's UID if authenticated, empty otherwise
     */
    public Optional<String> getCurrentUserId() {
        return getCurrentUser().map(FirebaseUserInfo::uid);
    }

    /**
     * Checks if there is currently an authenticated user.
     *
     * @return true if a user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    /**
     * Gets the current user or throws an exception if not authenticated.
     * Use this when authentication is required.
     *
     * @return FirebaseUserInfo of the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public FirebaseUserInfo requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }
}