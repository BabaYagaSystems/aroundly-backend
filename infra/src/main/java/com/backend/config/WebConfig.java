package com.backend.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration class for setting up Cross-Origin Resource Sharing (CORS) in the application.
 *
 * <p>CORS is a security feature implemented by browsers that blocks requests to your backend
 * from other origins unless explicitly allowed. This configuration allows the frontend
 * to make requests to this backend, including with credentials
 * like cookies or authorization headers.</p>
 *
 * <p>By defining this configuration, we ensure the frontend can interact with the API
 * without running into CORS errors in the browser.</p>
 */
@Configuration
public class WebConfig {

  @Value("${allowed.origins}")
  private String allowedOrigins;

  /**
   * Defines and registers the global CORS configuration for the entire application.
   *
   * <p>This method creates a {@link CorsConfiguration} that:
   * <ul>
   *   <li>Allows requests only from the production frontend domain</li>
   *   <li>Permits standard HTTP methods like GET, POST, PUT, etc.</li>
   *   <li>Accepts all request headers</li>
   *   <li>Allows sending credentials (e.g., cookies, auth tokens)</li>
   * </ul>
   *
   * <p>This configuration is applied to all request paths ("/**") via {@link UrlBasedCorsConfigurationSource}.</p>
   *
   * @return a {@link CorsConfigurationSource} bean that Spring will use to handle CORS
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigins));
    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

}
