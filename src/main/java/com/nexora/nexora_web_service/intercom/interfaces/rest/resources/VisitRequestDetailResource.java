package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

import java.time.LocalDateTime;

public record VisitRequestDetailResource(
    Long id,
    String visitorName,
    Long apartmentId,
    String status,
    String photoUrl,
    String audioUrl,
    LocalDateTime createdAt
) {}
