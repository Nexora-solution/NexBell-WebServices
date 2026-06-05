package com.nexora.nexora_web_service.directory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateResidentContactResource(
    @Schema(description = "New contact email address", example = "new.email@nexora.com")
    @NotBlank String email,
    @Schema(description = "New contact phone number", example = "+51999888666")
    @NotBlank String phone
) {}
