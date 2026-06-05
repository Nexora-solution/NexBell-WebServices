package com.nexora.nexora_web_service.intercom.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "intercom_queue_items")
public class IntercomQueueItem extends AuditableModel {

    @Column(name = "visit_request_id", nullable = false)
    private Long visitRequestId;

    @Column(name = "assigned_doorman_id")
    private Long assignedDoormanId;

    @Column(name = "enqueued_at", nullable = false)
    private LocalDateTime enqueuedAt;

    @Column(nullable = false)
    private String status = "ENQUEUED"; // ENQUEUED, PROCESSING, DEQUEUED

    public IntercomQueueItem() {
        this.enqueuedAt = LocalDateTime.now();
        this.status = "ENQUEUED";
    }

    public IntercomQueueItem(Long visitRequestId) {
        this.visitRequestId = visitRequestId;
        this.enqueuedAt = LocalDateTime.now();
        this.status = "ENQUEUED";
    }

    public Long getVisitRequestId() {
        return visitRequestId;
    }

    public void setVisitRequestId(Long visitRequestId) {
        this.visitRequestId = visitRequestId;
    }

    public Long getAssignedDoormanId() {
        return assignedDoormanId;
    }

    public void setAssignedDoormanId(Long assignedDoormanId) {
        this.assignedDoormanId = assignedDoormanId;
    }

    public LocalDateTime getEnqueuedAt() {
        return enqueuedAt;
    }

    public void setEnqueuedAt(LocalDateTime enqueuedAt) {
        this.enqueuedAt = enqueuedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void assignDoorman(Long doormanId) {
        this.assignedDoormanId = doormanId;
        this.status = "PROCESSING";
    }

    public void dequeue() {
        this.status = "DEQUEUED";
    }
}
