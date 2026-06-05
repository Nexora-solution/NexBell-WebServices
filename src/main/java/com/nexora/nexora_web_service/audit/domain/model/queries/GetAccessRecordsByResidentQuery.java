package com.nexora.nexora_web_service.audit.domain.model.queries;

public record GetAccessRecordsByResidentQuery(Long residentId) {
    public GetAccessRecordsByResidentQuery {
        if (residentId == null || residentId <= 0) throw new IllegalArgumentException("Resident ID must be positive");
    }
}
