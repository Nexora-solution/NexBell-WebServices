package com.nexora.nexora_web_service.security.domain.model.entities;

import com.nexora.nexora_web_service.security.domain.model.valueobjects.SensorType;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_alarms")
public class SecurityAlarm extends AuditableModel {

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;

    @Column(nullable = false)
    private String status = "TRIGGERED"; // TRIGGERED, ACKNOWLEDGED, RESOLVED

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    public SecurityAlarm() {
        this.status = "TRIGGERED";
        this.triggeredAt = LocalDateTime.now();
    }

    public SecurityAlarm(SensorType sensorType) {
        this.sensorType = sensorType;
        this.status = "TRIGGERED";
        this.triggeredAt = LocalDateTime.now();
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public void acknowledge() {
        this.status = "ACKNOWLEDGED";
    }

    public void resolve() {
        this.status = "RESOLVED";
    }
}
