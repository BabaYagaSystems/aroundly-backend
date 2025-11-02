package com.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Central Redis configuration exposing the {@link StringRedisTemplate} used across the application
 * for reaction state persistence and any other string-based Redis operations.
 */
@Configuration
public class RedisConfig {

  /**
   * Creates a {@link StringRedisTemplate} backed by the auto-configured {@link RedisConnectionFactory}.
   *
   * @param redisConnectionFactory connection factory managed by Spring Boot
   * @return reusable Redis template for string operations
   */
  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    return new StringRedisTemplate(redisConnectionFactory);
  }
}
