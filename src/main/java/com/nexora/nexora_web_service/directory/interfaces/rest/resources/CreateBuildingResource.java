package com.nexora.nexora_web_service.directory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateBuildingResource(
    @Schema(description = "Name of the building", example = "Nexora Residences")
    @NotBlank String name,
    @Schema(description = "Street address of the building", example = "Av. Javier Prado 1234")
    @NotBlank String address
) {}
