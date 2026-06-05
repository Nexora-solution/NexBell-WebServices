package com.nexora.nexora_web_service.directory.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ContactChannel(String email, String phone) {
    public ContactChannel {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
    }
}
