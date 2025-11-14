package com.backend.adapter.outbound.auth;

import com.backend.services.UserService;
import com.backend.domain.actor.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that intercepts HTTP requests and validates Firebase authentication tokens.
 * If a valid token is present, it populates the Spring Security context with user information
 * and syncs the user to the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

  private final UserService userService;
  private final TokenValidationService tokenValidationService;

  @Override
  protected void doFilterInternal(
      @NonNull final HttpServletRequest request,
      @NonNull final HttpServletResponse response,
      @NonNull final FilterChain filterChain) throws ServletException, IOException {

    try {
      final String authHeader = request.getHeader("Authorization");

      if (authHeader != null) {
        final Optional<String> token = tokenValidationService.extractToken(authHeader);

        if (token.isPresent()) {
          final Optional<User> userInfo = tokenValidationService.validateToken(token.get());

          if (userInfo.isPresent()) {
            try {
              userService.create(userInfo.get());
            } catch (Exception e) {
              log.error("Failed to sync user to database: {}", userInfo.get().uid(), e);
            }

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
  private void setAuthentication(final HttpServletRequest request, final User userInfo) {
    final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userInfo,  // Principal (the authenticated user)
        null,      // Credentials (not needed after authentication)
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    log.debug("Set authentication for user: {}", userInfo.uid());
  }
}
