package com.backend.adapter.outbound.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.domain.actor.Role;
import com.backend.domain.actor.User;
import com.backend.domain.actor.UserId;
import com.backend.services.UserService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

  @Mock private UserService userService;
  @Mock private TokenValidationService tokenValidationService;

  @InjectMocks private AuthenticationFilter authenticationFilter;

  @BeforeEach
  void setup() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterSetsAuthenticationWhenTokenValid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer token-value");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);
    User user = sampleUser();

    when(tokenValidationService.extractToken("Bearer token-value")).thenReturn(Optional.of("token-value"));
    when(tokenValidationService.validateToken("token-value")).thenReturn(Optional.of(user));

    authenticationFilter.doFilterInternal(request, response, chain);

    verify(userService).create(user);
    verify(chain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
  }

  @Test
  void doFilterSkipsWhenTokenInvalid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer bad-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    when(tokenValidationService.extractToken("Bearer bad-token")).thenReturn(Optional.of("bad-token"));
    when(tokenValidationService.validateToken("bad-token")).thenReturn(Optional.empty());

    authenticationFilter.doFilterInternal(request, response, chain);

    verify(userService, never()).create(org.mockito.ArgumentMatchers.any());
    verify(chain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void doFilterSkipsWhenAuthorizationHeaderMissing() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    authenticationFilter.doFilterInternal(request, response, chain);

    verify(userService, never()).create(org.mockito.ArgumentMatchers.any());
    verify(chain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  private User sampleUser() {
    return User.builder()
        .uid(new UserId("firebase-1"))
        .email("user@example.com")
        .name("User")
        .picture("pic")
        .role(Role.USER)
        .emailVerified(true)
        .deviceIdToken("token")
        .range(5)
        .build();
  }
}
