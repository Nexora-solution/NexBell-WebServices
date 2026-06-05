package com.nexora.nexora_web_service.audit.domain.model.commands;

import com.nexora.nexora_web_service.audit.domain.model.valueobjects.AccessDecision;

public record RegisterAccessRecordCommand(String correlationId, AccessDecision decision) {
    public RegisterAccessRecordCommand {
        if (correlationId == null || correlationId.isBlank()) throw new IllegalArgumentException("Correlation ID cannot be empty");
        if (decision == null) throw new IllegalArgumentException("Decision cannot be null");
    }
}
