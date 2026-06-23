package com.nexora.nexora_web_service.security.domain.model.valueobjects;

/**
 * Sensor types known to the security bounded context.
 *
 * SW420_VIBRATION  — legacy vibration sensor (replaced by MC38 magnetic sensor in v2)
 * ULTRASONIC       — HC-SR04 ultrasonic presence sensor
 * MC38_MAGNETIC    — MC38 magnetic door sensor (detects OPEN/CLOSED state changes)
 */
public enum SensorType {
    SW420_VIBRATION,
    ULTRASONIC,
    MC38_MAGNETIC
}
