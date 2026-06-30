package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreatePreRegisteredVisitResource(
    @Schema(description = "Resident profile ID", example = "1")
    @NotNull Long residentId,
    @Schema(description = "Visitor full name", example = "Patricia Ruiz")
    @NotBlank String visitorName,
    @Schema(description = "Visitor document number", example = "72938401")
    String visitorDocument,
    @Schema(description = "Visitor photo as a base64 data URL, e.g. 'data:image/jpeg;base64,...'")
    String visitorPhotoUrl,
    @Schema(description = "Expected date and time of the visit", example = "2026-06-10T15:00:00")
    @NotNull LocalDateTime expectedAt,
    @Schema(description = "Who registered this visit: RESIDENT (default, via mobile app) or DOORMAN (ad-hoc walk-in)", example = "RESIDENT")
    String registeredBy
) {}
