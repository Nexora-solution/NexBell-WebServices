package com.nexora.nexora_web_service.directory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResidentProfileResource(
    @Schema(description = "ID of the resident profile")
    Long id,
    @Schema(description = "User Account ID in IAM context")
    Long userId,
    @Schema(description = "Full name of the resident")
    String fullName,
    @Schema(description = "Document identity number")
    String documentNumber,
    @Schema(description = "Contact email address")
    String email,
    @Schema(description = "Contact phone number")
    String phone,
    @Schema(description = "Apartment code (only populated on GET /residents/{id})")
    String apartmentCode,
    @Schema(description = "ID of the apartment (needed to register a visit request)")
    Long apartmentId
) {}
