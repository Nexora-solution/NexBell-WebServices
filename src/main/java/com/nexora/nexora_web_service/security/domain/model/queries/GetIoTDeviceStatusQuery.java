package com.nexora.nexora_web_service.security.domain.model.queries;

public record GetIoTDeviceStatusQuery(String deviceCode) {
    public GetIoTDeviceStatusQuery {
        if (deviceCode == null || deviceCode.isBlank()) throw new IllegalArgumentException("Device code cannot be empty");
    }
}
