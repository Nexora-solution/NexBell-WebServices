package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetResource(
    @Schema(description = "Email address registered on Nexora", example = "user@nexora.com")
    @NotBlank @Email String email
) {}
