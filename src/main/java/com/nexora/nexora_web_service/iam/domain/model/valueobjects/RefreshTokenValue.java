package com.nexora.nexora_web_service.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record RefreshTokenValue(String token) {
    public RefreshTokenValue {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be empty");
        }
    }
}
