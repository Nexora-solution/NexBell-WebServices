package com.nexora.nexora_web_service.intercom.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record EvidenceUri(String uri) {
    public EvidenceUri {
        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("Evidence URI cannot be empty");
        }
    }
}
