package com.nexora.nexora_web_service.directory.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ContactChannel(String email, String phone) {
    public ContactChannel {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        // Phone is optional: a resident has no phone until they set it in the
        // mobile app on first login. Until then it stays empty.
        if (phone == null) phone = "";
    }
}
