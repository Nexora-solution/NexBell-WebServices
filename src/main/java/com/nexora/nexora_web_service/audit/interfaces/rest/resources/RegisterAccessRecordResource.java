package com.nexora.nexora_web_service.audit.interfaces.rest.resources;

import com.nexora.nexora_web_service.audit.domain.model.valueobjects.AccessDecision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterAccessRecordResource(
    @Schema(description = "Correlation ID of the request", example = "1")
    @NotBlank String correlationId,
    @Schema(description = "Decision outcome", example = "APPROVED")
    @NotNull AccessDecision decision
) {}
