package com.nexora.nexora_web_service.intercom.domain.model.commands;

public record AttachVisitorEvidenceCommand(Long visitRequestId, String photoUrl, String audioUrl) {
    public AttachVisitorEvidenceCommand {
        if (visitRequestId == null || visitRequestId <= 0) throw new IllegalArgumentException("Visit request ID must be positive");
        if (photoUrl == null || photoUrl.isBlank()) throw new IllegalArgumentException("Photo URL cannot be empty");
    }
}
