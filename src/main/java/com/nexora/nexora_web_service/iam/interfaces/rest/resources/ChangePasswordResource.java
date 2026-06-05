package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordResource(
    @Schema(description = "User's email address", example = "user@nexora.com")
    @NotBlank String email,
    @Schema(description = "User's current password", example = "oldPassword123")
    @NotBlank String oldPassword,
    @Schema(description = "User's new password", example = "newPassword123")
    @NotBlank String newPassword
) {}
