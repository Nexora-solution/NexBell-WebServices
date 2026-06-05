package com.nexora.nexora_web_service.iam.domain.model.commands;

public record RevokeSessionCommand(String token) {
    public RevokeSessionCommand {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Token cannot be null or empty");
    }
}
