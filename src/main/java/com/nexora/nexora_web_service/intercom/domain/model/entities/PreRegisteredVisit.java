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

    // Visitor photo the resident attaches when pre-registering, as a base64
    // data URL (same pattern as BuildingDirectory.imageUrl). Optional.
    @Column(name = "visitor_photo_url", columnDefinition = "TEXT")
    private String visitorPhotoUrl;

    @Column(name = "expected_at", nullable = false)
    private LocalDateTime expectedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /** PENDING, APPROVED, REJECTED — the doorman/resident decision lifecycle for this pre-registered visit. */
    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    /**
     * "RESIDENT" — the resident pre-registered this visitor themselves from
     * the mobile app, so it belongs in the doorman's "pre-registered visits"
     * sidebar while pending.
     * "DOORMAN" — the doorman registered this on the fly for a walk-in
     * visitor with no prior pre-registration. It's notified immediately and
     * never shown in that sidebar — it only shows up in history once decided.
     */
    @Column(name = "registered_by", nullable = false)
    private String registeredBy = "RESIDENT";

    public PreRegisteredVisit() {
        this.isActive = true;
        this.status = "PENDING";
    }

    public PreRegisteredVisit(Long residentId, String visitorName, String visitorDocument, LocalDateTime expectedAt) {
        this(residentId, visitorName, visitorDocument, null, expectedAt, "RESIDENT");
    }

    public PreRegisteredVisit(Long residentId, String visitorName, String visitorDocument, String visitorPhotoUrl, LocalDateTime expectedAt, String registeredBy) {
        this.residentId = residentId;
        this.visitorName = visitorName;
        this.visitorDocument = visitorDocument;
        this.visitorPhotoUrl = visitorPhotoUrl;
        this.expectedAt = expectedAt;
        this.isActive = true;
        this.status = "PENDING";
        this.registeredBy = registeredBy != null ? registeredBy : "RESIDENT";
    }

    public String getVisitorPhotoUrl() {
        return visitorPhotoUrl;
    }

    public void setVisitorPhotoUrl(String visitorPhotoUrl) {
        this.visitorPhotoUrl = visitorPhotoUrl;
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

    public void update(String visitorName, String visitorDocument, String visitorPhotoUrl, LocalDateTime expectedAt) {
        this.visitorName = visitorName;
        this.visitorDocument = visitorDocument;
        this.visitorPhotoUrl = visitorPhotoUrl;
        this.expectedAt = expectedAt;
    }

    public void cancel() {
        this.isActive = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expectedAt);
    }

    public String getStatus() {
        return status;
    }

    public void registerDecision(String decision) {
        this.status = decision; // "APPROVED" or "REJECTED"
    }

    public String getRegisteredBy() {
        return registeredBy;
    }
}
