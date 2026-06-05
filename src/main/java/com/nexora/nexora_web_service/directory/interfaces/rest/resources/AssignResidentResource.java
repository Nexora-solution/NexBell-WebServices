package com.nexora.nexora_web_service.directory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignResidentResource(
    @Schema(description = "User account ID in IAM context", example = "1")
    @NotNull Long userId,
    @Schema(description = "Full name of the resident", example = "Juan Perez")
    @NotBlank String fullName,
    @Schema(description = "Document identity number of the resident", example = "73849501")
    @NotBlank String documentNumber,
    @Schema(description = "Resident's contact email address", example = "juan.perez@nexora.com")
    @NotBlank String email,
    @Schema(description = "Resident's contact phone number", example = "+51999888777")
    @NotBlank String phone
) {}
