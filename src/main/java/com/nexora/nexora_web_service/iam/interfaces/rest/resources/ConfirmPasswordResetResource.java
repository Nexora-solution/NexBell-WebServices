package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ConfirmPasswordResetResource(
    @Schema(description = "Password reset token received", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotBlank String token,
    @Schema(description = "The new password to set", example = "newPassword123")
    @NotBlank String newPassword
) {}
