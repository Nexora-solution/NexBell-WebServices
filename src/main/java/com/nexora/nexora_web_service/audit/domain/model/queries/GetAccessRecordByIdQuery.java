package com.nexora.nexora_web_service.audit.domain.model.queries;

public record GetAccessRecordByIdQuery(Long id) {
    public GetAccessRecordByIdQuery {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID must be positive");
    }
}
