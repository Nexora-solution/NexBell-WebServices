package com.nexora.nexora_web_service.audit.interfaces.rest.resources;

import com.nexora.nexora_web_service.audit.domain.model.valueobjects.AccessDecision;
import java.time.LocalDateTime;
import java.util.List;

public record AccessRecordResource(
    Long id,
    String correlationId,
    AccessDecision decision,
    boolean isSealed,
    List<String> timeline,
    LocalDateTime createdAt
) {}
