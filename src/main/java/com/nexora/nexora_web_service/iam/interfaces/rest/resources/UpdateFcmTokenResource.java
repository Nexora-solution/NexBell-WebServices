package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateFcmTokenResource(
    @Schema(description = "Firebase Cloud Messaging device token for push notifications")
    @NotBlank String fcmToken
) {}
