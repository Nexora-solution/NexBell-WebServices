package com.nexora.nexora_web_service.directory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateResidentContactResource(
    @Schema(description = "Full name of the resident")
    String fullName,
    @Schema(description = "New contact email address")
    String email,
    @Schema(description = "New contact phone number")
    String phone,
    @Schema(description = "New apartment code (e.g. 101, 202)")
    String apartmentCode,
    @Schema(description = "New profile photo as a base64 data URL")
    String photoUrl
) {}
