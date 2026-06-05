package com.nexora.nexora_web_service.intercom.domain.model.commands;

import java.time.LocalDateTime;

public record UpdatePreRegisteredVisitCommand(Long id, String visitorName, String visitorDocument, LocalDateTime expectedAt) {
    public UpdatePreRegisteredVisitCommand {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID must be positive");
        if (visitorName == null || visitorName.isBlank()) throw new IllegalArgumentException("Visitor name cannot be empty");
        if (expectedAt == null) throw new IllegalArgumentException("Expected date cannot be null");
    }
}
