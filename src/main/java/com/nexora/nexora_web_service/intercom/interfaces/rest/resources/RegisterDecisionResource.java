package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RegisterDecisionResource(
    @Schema(description = "Decision to make for the visit request", example = "APPROVED")
    @NotBlank String decision
) {}
