package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticatedUserResource(
    @Schema(description = "User's unique ID")
    Long id,
    @Schema(description = "User's email address")
    String email,
    @Schema(description = "JWT Access Token")
    String accessToken,
    @Schema(description = "Refresh Token")
    String refreshToken
) {}
