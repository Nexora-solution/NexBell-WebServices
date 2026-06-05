package com.nexora.nexora_web_service.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record EmailAddress(String email) {
    public EmailAddress {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
