package com.nexora.nexora_web_service.intercom.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Initialises the Firebase Admin SDK once at application startup, using the
 * service account credentials file. If the file is missing (e.g. a fresh
 * checkout without Firebase configured yet), push notifications are simply
 * disabled instead of crashing the whole backend.
 */
@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    private final ResourcePatternResolver resourceResolver;

    public FirebaseConfig(ResourcePatternResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return; // already initialised (e.g. in tests)
        }

        try {
            Resource resource = resourceResolver.getResource(credentialsPath);
            if (!resource.exists()) {
                log.warn("Firebase credentials file not found at '{}'. Push notifications are disabled.", credentialsPath);
                return;
            }

            try (var inputStream = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialised successfully.");
            }
        } catch (IOException e) {
            log.error("Failed to initialise Firebase Admin SDK: {}", e.getMessage());
        }
    }
}
