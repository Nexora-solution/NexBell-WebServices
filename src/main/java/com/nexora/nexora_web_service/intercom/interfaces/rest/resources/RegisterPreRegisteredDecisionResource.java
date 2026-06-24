package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RegisterPreRegisteredDecisionResource(
    @Schema(description = "Resident's decision: APPROVED or REJECTED", example = "APPROVED")
    @NotBlank String decision
) {}
