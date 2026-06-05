package com.nexora.nexora_web_service.directory.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ApartmentCode(String code) {
    public ApartmentCode {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Apartment code cannot be empty");
        }
    }
}
