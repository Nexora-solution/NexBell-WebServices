package com.nexora.nexora_web_service.intercom.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

@Entity
@Table(name = "visit_requests")
public class VisitRequest extends AuditableModel {

    @Column(name = "visitor_name", nullable = false)
    private String visitorName;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, NOTIFIED, APPROVED, REJECTED, CLOSED

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "evidence_id")
    private VisitorEvidence evidence;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "notification_id")
    private NotificationDispatch notification;

    public VisitRequest() {
        this.status = "PENDING";
    }

    public VisitRequest(String visitorName, Long apartmentId) {
        this.visitorName = visitorName;
        this.apartmentId = apartmentId;
        this.status = "PENDING";
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public VisitorEvidence getEvidence() {
        return evidence;
    }

    public void setEvidence(VisitorEvidence evidence) {
        this.evidence = evidence;
    }

    public NotificationDispatch getNotification() {
        return notification;
    }

    public void setNotification(NotificationDispatch notification) {
        this.notification = notification;
    }

    public void attachEvidence(VisitorEvidence evidence) {
        this.evidence = evidence;
    }

    public void markNotified(NotificationDispatch notification) {
        this.notification = notification;
        this.status = "NOTIFIED";
    }

    public void registerDecision(String decision) {
        this.status = decision;
    }

    public void close() {
        this.status = "CLOSED";
    }
}
