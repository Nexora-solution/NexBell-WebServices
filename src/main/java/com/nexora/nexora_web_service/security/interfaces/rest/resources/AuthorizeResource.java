package com.nexora.nexora_web_service.security.interfaces.rest.resources;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthorizeResource(
    @Schema(description = "User's system role", example = "DOORMAN")
    @NotNull RoleName role,
    @Schema(description = "The permission code to check", example = "CAN_AUTHORIZE")
    @NotBlank String permissionCode
) {}
