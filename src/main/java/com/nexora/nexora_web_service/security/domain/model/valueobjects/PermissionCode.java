package com.nexora.nexora_web_service.security.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PermissionCode(String code) {
    public PermissionCode {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Permission code cannot be empty");
        }
    }
}
