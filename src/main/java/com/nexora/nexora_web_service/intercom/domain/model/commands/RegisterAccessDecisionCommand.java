package com.nexora.nexora_web_service.intercom.domain.model.commands;

public record RegisterAccessDecisionCommand(Long visitRequestId, String decision) {
    public RegisterAccessDecisionCommand {
        if (visitRequestId == null || visitRequestId <= 0) throw new IllegalArgumentException("Visit request ID must be positive");
        if (decision == null || (!decision.equalsIgnoreCase("APPROVED") && !decision.equalsIgnoreCase("REJECTED"))) {
            throw new IllegalArgumentException("Decision must be either APPROVED or REJECTED");
        }
    }
}
