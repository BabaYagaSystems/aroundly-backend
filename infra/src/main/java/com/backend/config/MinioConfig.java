package com.backend.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for initializing and exposing MinIO-related beans in the Spring context.
 *
 * <p>MinIO is an object storage service compatible with Amazon S3. This configuration
 * sets up the {@link io.minio.MinioClient} bean, which allows the application to
 * interact with MinIO for file uploads, downloads, and bucket management.</p>
 *
 * <p>It also exposes the bucket name defined in application properties as a bean,
 * so that it can be easily injected into services or components that need to reference it.</p>
 *
 * <p>The configuration values (endpoint, access key, secret key, bucket name)
 * are loaded from {@link MinioProperties}, which is populated from
 * the application’s configuration files (e.g., <code>application.yml</code>).</p>
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

  /**
   * Creates and configures a {@link MinioClient} bean using the provided MinIO properties.
   *
   * <p>This client is used to perform all operations against the MinIO server,
   * such as uploading, retrieving, and deleting objects.</p>
   *
   * @param properties the MinIO configuration properties (endpoint, access key, secret key)
   * @return a configured {@link MinioClient} instance ready for use
   */
  @Bean
  public MinioClient minioClient(MinioProperties properties) {
    return MinioClient.builder()
      .endpoint(properties.getEndpoint())
      .credentials(properties.getAccessKey(), properties.getSecretKey())
      .build();

  }

  /**
   * Exposes the MinIO bucket name as a Spring bean.
   *
   * <p>This allows other beans (such as storage services) to inject the bucket name directly,
   * instead of hardcoding it or fetching it repeatedly from configuration files.</p>
   *
   * @param properties the MinIO configuration properties containing the bucket name
   * @return the bucket name defined in application properties
   */
  @Bean
  public String minioBucket(MinioProperties properties) {
    return properties.getBucket();
  }

}
