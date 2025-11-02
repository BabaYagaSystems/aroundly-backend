package com.backend.adapter.inbound.security;

import com.backend.adapter.outbound.repo.persistence.UserSyncService;
import com.backend.domain.actor.FirebaseUserInfo;
import com.backend.services.FirebaseTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Filter that intercepts HTTP requests and validates Firebase authentication tokens.
 * If a valid token is present, it populates the Spring Security context with user information
 * and syncs the user to the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseTokenValidator tokenValidator;
    private final UserSyncService userSyncService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null) {
                Optional<String> token = tokenValidator.extractToken(authHeader);

                if (token.isPresent()) {
                    Optional<FirebaseUserInfo> userInfo = tokenValidator.validateToken(token.get());

                    if (userInfo.isPresent()) {
                        // Sync user to database (creates if doesn't exist, updates last login if exists)
                        try {
                            userSyncService.syncUser(userInfo.get());
                        } catch (Exception e) {
                            log.error("Failed to sync user to database: {}", userInfo.get().uid(), e);
                            // Continue anyway - authentication still valid
                        }

                        // Set authentication in security context
                        setAuthentication(request, userInfo.get());
                    } else {
                        log.debug("Invalid or expired Firebase token");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing Firebase authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Sets the authentication in Spring Security context.
     */
    private void setAuthentication(HttpServletRequest request, FirebaseUserInfo userInfo) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userInfo,  // Principal (the authenticated user)
                null,      // Credentials (not needed after authentication)
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Set authentication for user: {}", userInfo.uid());
    }
}