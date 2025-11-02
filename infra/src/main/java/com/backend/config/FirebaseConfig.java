package com.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration class that initializes Firebase Admin SDK.
 * Loads the service account JSON file and initializes FirebaseApp.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-file:firebase-service-account.json}")
    private String serviceAccountFile;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Try to load from classpath (resources folder)
//                InputStream serviceAccount = new ClassPathResource(serviceAccountFile).getInputStream();

              InputStream serviceAccountStream;

              if (serviceAccountFile.trim().startsWith("{")) {
                log.info("Initializing Firebase using JSON from environment variable...");
                serviceAccountStream =
                    new ByteArrayInputStream(serviceAccountFile.getBytes(StandardCharsets.UTF_8));
              } else {
                // 🔹 Otherwise, treat it as a classpath file (for local dev)
                log.info("Initializing Firebase using file from classpath: {}", serviceAccountFile);
                serviceAccountStream = new ClassPathResource(serviceAccountFile).getInputStream();
              }


              FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            } else {
                log.info("Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK. Make sure the service account file exists at: {}",
                    serviceAccountFile, e);
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }
}