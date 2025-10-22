package com.backend.config;

import com.backend.adapter.outbound.storage.MinioObjectStorageAdapter;
import com.backend.port.outbound.storage.ObjectStoragePort;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

  @Bean
  public MinioClient minioClient(MinioProperties properties) {
    return MinioClient.builder()
      .endpoint(properties.getEndpoint())
      .credentials(properties.getAccessKey(), properties.getSecretKey())
      .build();

  }

  @Bean
  public String minioBucket(MinioProperties properties) {
    return properties.getBucket();
  }

//  @Bean
//  public ObjectStoragePort objectStorageRepository(
//      MinioClient client, MinioProperties properties) {
//
//    return new MinioObjectStorageAdapter(client, properties.getBucket());
//  }
}
