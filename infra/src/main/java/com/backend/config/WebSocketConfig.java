package com.backend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Enables STOMP-over-WebSocket support for the application and defines the endpoints/prefixes
 * the clients interact with.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * Configures the message broker prefixes, separating client-to-server traffic from broadcast
   * topics served by the simple in-memory broker.
   */
  @Override
  public void configureMessageBroker(@NotNull final MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic"); // server -> client
    registry.setApplicationDestinationPrefixes("/aroundly"); // client -> server
  }

  /**
   * Registers the SockJS-enabled STOMP endpoint that browsers connect to for incident updates.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/incident-websocket")
        .withSockJS();
  }
}
