package com.nexora.nexora_web_service.security.domain.model.commands;

/**
 * Command issued when the MC38 magnetic sensor reports a change in the
 * physical door state (OPEN / CLOSED). This is a read-only telemetry event:
 * it only records the door's current state, it does not actuate anything.
 */
public record UpdateDoorStateCommand(String state) {}
