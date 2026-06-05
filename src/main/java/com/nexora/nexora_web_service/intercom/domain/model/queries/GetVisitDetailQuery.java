package com.nexora.nexora_web_service.intercom.domain.model.queries;

public record GetVisitDetailQuery(Long visitRequestId) {
    public GetVisitDetailQuery {
        if (visitRequestId == null || visitRequestId <= 0) throw new IllegalArgumentException("Visit request ID must be positive");
    }
}
