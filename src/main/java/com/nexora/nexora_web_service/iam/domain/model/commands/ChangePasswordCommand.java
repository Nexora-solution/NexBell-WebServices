package com.nexora.nexora_web_service.iam.domain.model.commands;

public record ChangePasswordCommand(String email, String oldPassword, String newPassword) {
    public ChangePasswordCommand {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email cannot be null or empty");
        if (oldPassword == null || oldPassword.isBlank()) throw new IllegalArgumentException("Old password cannot be null or empty");
        if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("New password cannot be null or empty");
    }
}
