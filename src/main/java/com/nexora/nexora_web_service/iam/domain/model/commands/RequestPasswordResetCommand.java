package com.nexora.nexora_web_service.iam.domain.model.commands;

public record RequestPasswordResetCommand(String email) {
    public RequestPasswordResetCommand {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email cannot be null or empty");
    }
}
