package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AttachEvidenceResource(
    @Schema(description = "Public URL of the captured visitor photo", example = "https://storage.nexora.com/photo/123.jpg")
    @NotBlank String photoUrl,
    @Schema(description = "Public URL of the recorded visitor audio message", example = "https://storage.nexora.com/audio/123.mp3")
    String audioUrl
) {}
