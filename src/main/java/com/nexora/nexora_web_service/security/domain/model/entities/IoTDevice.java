package com.nexora.nexora_web_service.security.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "iot_devices")
public class IoTDevice extends AuditableModel {

    @Column(name = "device_code", nullable = false, unique = true)
    private String deviceCode;

    @Column(name = "is_camera_enabled", nullable = false)
    private boolean isCameraEnabled = true;

    @Column(name = "is_microphone_enabled", nullable = false)
    private boolean isMicrophoneEnabled = true;

    @Column(nullable = false)
    private String status = "ONLINE"; // ONLINE, OFFLINE

    @Column(name = "door_state", nullable = false)
    private String doorState = "CLOSED"; // OPEN, CLOSED — physical state read from the MC38 magnetic sensor

    @Column(name = "door_state_changed_at")
    private LocalDateTime doorStateChangedAt;

    public IoTDevice() {
        this.isCameraEnabled = true;
        this.isMicrophoneEnabled = true;
        this.status = "ONLINE";
        this.doorState = "CLOSED";
    }

    public IoTDevice(String deviceCode) {
        this.deviceCode = deviceCode;
        this.isCameraEnabled = true;
        this.isMicrophoneEnabled = true;
        this.status = "ONLINE";
        this.doorState = "CLOSED";
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public boolean isCameraEnabled() {
        return isCameraEnabled;
    }

    public void setCameraEnabled(boolean cameraEnabled) {
        isCameraEnabled = cameraEnabled;
    }

    public boolean isMicrophoneEnabled() {
        return isMicrophoneEnabled;
    }

    public void setMicrophoneEnabled(boolean microphoneEnabled) {
        isMicrophoneEnabled = microphoneEnabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void toggleMedia(boolean camera, boolean microphone) {
        this.isCameraEnabled = camera;
        this.isMicrophoneEnabled = microphone;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public String getDoorState() {
        return doorState;
    }

    public LocalDateTime getDoorStateChangedAt() {
        return doorStateChangedAt;
    }

    /**
     * Updates the physical door state reported by the MC38 magnetic sensor.
     * Only "OPEN" / "CLOSED" are accepted; the change timestamp is refreshed
     * only when the state actually transitions.
     */
    public void updateDoorState(String newState) {
        String normalized = newState == null ? "" : newState.trim().toUpperCase();
        if (!normalized.equals("OPEN") && !normalized.equals("CLOSED")) {
            throw new IllegalArgumentException("Door state must be OPEN or CLOSED");
        }
        if (!normalized.equals(this.doorState)) {
            this.doorStateChangedAt = LocalDateTime.now();
        }
        this.doorState = normalized;
    }
}
