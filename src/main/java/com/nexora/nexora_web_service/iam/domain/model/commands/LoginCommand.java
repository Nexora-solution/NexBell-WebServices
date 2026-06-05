package com.nexora.nexora_web_service.iam.domain.model.commands;

public record LoginCommand(String email, String password) {
    public LoginCommand {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email cannot be null or empty");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password cannot be null or empty");
    }
}
