package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateVisitRequestResource(
    @Schema(description = "Visitor full name", example = "Carlos Gomez")
    @NotBlank String visitorName,
    @Schema(description = "ID of the target apartment", example = "1")
    @NotNull Long apartmentId
) {}
