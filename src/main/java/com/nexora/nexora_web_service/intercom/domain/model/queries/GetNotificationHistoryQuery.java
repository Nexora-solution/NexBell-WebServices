package com.nexora.nexora_web_service.intercom.domain.model.queries;

public record GetNotificationHistoryQuery(Long apartmentId) {
    public GetNotificationHistoryQuery {
        if (apartmentId == null || apartmentId <= 0) throw new IllegalArgumentException("Apartment ID must be positive");
    }
}
