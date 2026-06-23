package com.nexora.nexora_web_service.security.interfaces.rest.resources;

public record DoorStatusResource(
        String status,
        boolean online
) {}
