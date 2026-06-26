package com.nexora.nexora_web_service.security.interfaces.rest.resources;

import java.time.LocalDateTime;

/**
 * A security alarm event (e.g. door tampering / forced entry) exposed to the
 * doorman web app.
 */
public record SecurityAlarmResource(
        Long id,
        String sensorType,
        String status,
        LocalDateTime triggeredAt
) {}
