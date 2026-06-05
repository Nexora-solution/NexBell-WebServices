package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshSessionResource(
    @Schema(description = "The active refresh token to rotate", example = "8a32b210-9112-42da-9204-517be678f24b")
    @NotBlank String refreshToken
) {}
