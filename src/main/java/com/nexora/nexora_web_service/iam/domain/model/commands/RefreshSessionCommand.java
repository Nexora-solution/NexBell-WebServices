package com.nexora.nexora_web_service.iam.domain.model.commands;

public record RefreshSessionCommand(String refreshToken) {
    public RefreshSessionCommand {
        if (refreshToken == null || refreshToken.isBlank()) throw new IllegalArgumentException("Refresh token cannot be null or empty");
    }
}
