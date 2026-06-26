package com.nexora.nexora_web_service.security.interfaces.rest.resources;

import java.time.LocalDateTime;

/**
 * Current physical state of the door as read from the MC38 magnetic sensor.
 * `state` is "OPEN" or "CLOSED". This is informational only — it does not
 * control any actuator.
 */
public record DoorPhysicalStateResource(
        String state,
        LocalDateTime changedAt,
        boolean online
) {}
