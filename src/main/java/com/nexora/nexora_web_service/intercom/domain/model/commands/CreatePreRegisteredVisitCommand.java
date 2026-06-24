package com.nexora.nexora_web_service.intercom.domain.model.commands;

import java.time.LocalDateTime;

public record CreatePreRegisteredVisitCommand(Long residentId, String visitorName, String visitorDocument, LocalDateTime expectedAt, String registeredBy) {
    public CreatePreRegisteredVisitCommand {
        if (residentId == null || residentId <= 0) throw new IllegalArgumentException("Resident ID must be positive");
        if (visitorName == null || visitorName.isBlank()) throw new IllegalArgumentException("Visitor name cannot be empty");
        if (expectedAt == null) throw new IllegalArgumentException("Expected date cannot be null");
        if (registeredBy == null || registeredBy.isBlank()) registeredBy = "RESIDENT";
    }
}
