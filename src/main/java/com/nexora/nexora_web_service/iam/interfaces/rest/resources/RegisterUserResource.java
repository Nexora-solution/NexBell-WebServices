package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserResource(
    @Schema(description = "User's email address", example = "user@nexora.com")
    @NotBlank @Email String email,

    @Schema(description = "User's password", example = "password123")
    @NotBlank String password,

    @Schema(description = "User's role", example = "RESIDENT")
    @NotNull RoleName role
) {}
