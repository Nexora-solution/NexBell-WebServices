package com.nexora.nexora_web_service.security.domain.model.commands;

public record ToggleDeviceMediaStreamCommand(String deviceCode, boolean camera, boolean microphone) {
    public ToggleDeviceMediaStreamCommand {
        if (deviceCode == null || deviceCode.isBlank()) throw new IllegalArgumentException("Device code cannot be empty");
    }
}
