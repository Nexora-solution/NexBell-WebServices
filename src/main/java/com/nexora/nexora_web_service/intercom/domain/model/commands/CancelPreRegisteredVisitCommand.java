package com.nexora.nexora_web_service.intercom.domain.model.commands;

public record CancelPreRegisteredVisitCommand(Long id) {
    public CancelPreRegisteredVisitCommand {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID must be positive");
    }
}
