package com.nexora.nexora_web_service.security.interfaces.rest.resources;

public record IoTDeviceMediaStatusResource(
        String deviceCode,
        boolean cameraEnabled,
        boolean microphoneEnabled
) {}
