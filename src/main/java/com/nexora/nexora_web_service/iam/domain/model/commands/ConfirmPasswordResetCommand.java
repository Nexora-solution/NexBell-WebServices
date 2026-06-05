package com.nexora.nexora_web_service.iam.domain.model.commands;

public record ConfirmPasswordResetCommand(String token, String newPassword) {
    public ConfirmPasswordResetCommand {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Token cannot be null or empty");
        if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("New password cannot be null or empty");
    }
}
