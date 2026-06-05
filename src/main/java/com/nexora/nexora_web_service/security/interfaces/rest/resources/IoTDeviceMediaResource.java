package com.nexora.nexora_web_service.security.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record IoTDeviceMediaResource(
    @Schema(description = "The unique code of the IoT device", example = "DEV-ESP32-DOOR01")
    @NotBlank String deviceCode,
    @Schema(description = "Toggle status of the camera stream", example = "false")
    boolean camera,
    @Schema(description = "Toggle status of the microphone stream", example = "false")
    boolean microphone
) {}
