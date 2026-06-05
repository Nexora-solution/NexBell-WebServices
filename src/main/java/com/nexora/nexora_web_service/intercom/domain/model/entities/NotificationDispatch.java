package com.nexora.nexora_web_service.intercom.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_dispatches")
public class NotificationDispatch extends AuditableModel {

    @Column(nullable = false)
    private String status = "SCHEDULED"; // SCHEDULED, SENT, DELIVERED, FAILED

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean isRead = false;

    public NotificationDispatch() {
        this.status = "SCHEDULED";
        this.isRead = false;
    }

    public NotificationDispatch(String status) {
        this.status = status;
        this.isRead = false;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void markSent() {
        this.status = "SENT";
        this.sentAt = LocalDateTime.now();
    }

    public void markDelivered() {
        this.status = "DELIVERED";
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public void markRead() {
        this.isRead = true;
    }
}
