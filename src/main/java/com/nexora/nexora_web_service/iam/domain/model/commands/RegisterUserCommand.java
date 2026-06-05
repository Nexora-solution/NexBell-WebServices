package com.nexora.nexora_web_service.iam.domain.model.commands;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;

public record RegisterUserCommand(String email, String password, RoleName role) {
    public RegisterUserCommand {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email cannot be null or empty");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password cannot be null or empty");
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
    }
}
