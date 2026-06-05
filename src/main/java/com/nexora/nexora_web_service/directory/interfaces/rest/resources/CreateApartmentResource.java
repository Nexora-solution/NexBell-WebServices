package com.nexora.nexora_web_service.directory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateApartmentResource(
    @Schema(description = "ID of the building", example = "1")
    @NotNull Long buildingId,
    @Schema(description = "Apartment unit code/label", example = "101")
    @NotBlank String code
) {}
