package com.nexora.nexora_web_service.audit.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record CorrelationId(String correlationId) {
    public CorrelationId {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Correlation ID cannot be empty");
        }
    }
}
