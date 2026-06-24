package com.nexora.nexora_web_service.intercom.domain.model.commands;

public record RegisterPreRegisteredDecisionCommand(Long preRegisteredVisitId, String decision) {
    public RegisterPreRegisteredDecisionCommand {
        if (preRegisteredVisitId == null || preRegisteredVisitId <= 0) throw new IllegalArgumentException("Pre-registered visit ID must be positive");
        if (decision == null || (!decision.equalsIgnoreCase("APPROVED") && !decision.equalsIgnoreCase("REJECTED"))) {
            throw new IllegalArgumentException("Decision must be either APPROVED or REJECTED");
        }
    }
}
