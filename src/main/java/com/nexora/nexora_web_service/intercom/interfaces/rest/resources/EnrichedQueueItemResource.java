package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

public record EnrichedQueueItemResource(
        Long id,
        Long visitRequestId,
        String visitorName,
        String dni,
        String apartmentCode,
        String type,
        String status,
        String enqueuedAt
) {}
