package com.nexora.nexora_web_service.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PasswordHash(String passwordHash) {
    public PasswordHash {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
    }
}
