package com.backend.adapter.outbound.auth;

import com.backend.domain.actor.ActorId;
import com.backend.domain.actor.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
final class TokenValidationService {

  /**
   * Validates a Firebase ID token and extracts user information.
   *
   * @param idToken the Firebase ID token from the Authorization header
   * @return Optional containing FirebaseUserInfo if token is valid, empty otherwise
   */
  static Optional<User> validateToken(String idToken) {
    try {
      FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

      User userInfo = User.builder()
          .uid(new ActorId(decodedToken.getUid()))
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
  static Optional<String> extractToken(String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return Optional.of(authHeader.substring(7));
    }
    return Optional.empty();
  }
}
