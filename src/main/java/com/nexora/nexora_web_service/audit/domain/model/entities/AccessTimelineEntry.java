package com.nexora.nexora_web_service.audit.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_timeline_entries")
public class AccessTimelineEntry extends AuditableModel {

    @Column(name = "event_description", nullable = false)
    private String eventDescription;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AccessTimelineEntry() {}

    public AccessTimelineEntry(String eventDescription) {
        this.eventDescription = eventDescription;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
