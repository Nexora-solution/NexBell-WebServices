package com.nexora.nexora_web_service.intercom.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "pre_registered_visits")
public class PreRegisteredVisit extends AuditableModel {

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "visitor_name", nullable = false)
    private String visitorName;

    @Column(name = "visitor_document")
    private String visitorDocument;

    @Column(name = "expected_at", nullable = false)
    private LocalDateTime expectedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public PreRegisteredVisit() {
        this.isActive = true;
    }

    public PreRegisteredVisit(Long residentId, String visitorName, String visitorDocument, LocalDateTime expectedAt) {
        this.residentId = residentId;
        this.visitorName = visitorName;
        this.visitorDocument = visitorDocument;
        this.expectedAt = expectedAt;
        this.isActive = true;
    }

    public Long getResidentId() {
        return residentId;
    }

    public void setResidentId(Long residentId) {
        this.residentId = residentId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorDocument() {
        return visitorDocument;
    }

    public void setVisitorDocument(String visitorDocument) {
        this.visitorDocument = visitorDocument;
    }

    public LocalDateTime getExpectedAt() {
        return expectedAt;
    }

    public void setExpectedAt(LocalDateTime expectedAt) {
        this.expectedAt = expectedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void update(String visitorName, String visitorDocument, LocalDateTime expectedAt) {
        this.visitorName = visitorName;
        this.visitorDocument = visitorDocument;
        this.expectedAt = expectedAt;
    }

    public void cancel() {
        this.isActive = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expectedAt);
    }
}
