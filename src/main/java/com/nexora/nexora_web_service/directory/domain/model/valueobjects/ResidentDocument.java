package com.nexora.nexora_web_service.directory.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ResidentDocument(String documentNumber) {
    public ResidentDocument {
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("Document number cannot be empty");
        }
    }
}
