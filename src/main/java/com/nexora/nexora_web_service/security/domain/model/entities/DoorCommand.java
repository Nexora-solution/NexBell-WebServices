package com.nexora.nexora_web_service.security.domain.model.entities;

import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "door_commands")
public class DoorCommand extends AuditableModel {

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false)
    private CommandType commandType;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, DISPATCHED, CONFIRMED, FAILED

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public DoorCommand() {
        this.status = "PENDING";
    }

    public DoorCommand(CommandType commandType) {
        this.commandType = commandType;
        this.status = "PENDING";
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(LocalDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public void markDispatched() {
        this.status = "DISPATCHED";
        this.dispatchedAt = LocalDateTime.now();
    }

    public void markConfirmed() {
        this.status = "CONFIRMED";
        this.confirmedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}
