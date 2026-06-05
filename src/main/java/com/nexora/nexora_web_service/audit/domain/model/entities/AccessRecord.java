package com.nexora.nexora_web_service.audit.domain.model.entities;

import com.nexora.nexora_web_service.audit.domain.model.valueobjects.AccessDecision;
import com.nexora.nexora_web_service.audit.domain.model.valueobjects.CorrelationId;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "access_records")
public class AccessRecord extends AuditableModel {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "correlationId", column = @Column(name = "correlation_id", nullable = false, unique = true))
    })
    private CorrelationId correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessDecision decision;

    @Column(name = "is_sealed", nullable = false)
    private boolean isSealed = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "access_record_id")
    private List<AccessTimelineEntry> timeline = new ArrayList<>();

    public AccessRecord() {
        this.timeline = new ArrayList<>();
        this.isSealed = false;
    }

    public AccessRecord(CorrelationId correlationId, AccessDecision decision) {
        this.correlationId = correlationId;
        this.decision = decision;
        this.timeline = new ArrayList<>();
        this.isSealed = false;
    }

    public CorrelationId getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(CorrelationId correlationId) {
        this.correlationId = correlationId;
    }

    public AccessDecision getDecision() {
        return decision;
    }

    public void setDecision(AccessDecision decision) {
        this.decision = decision;
    }

    public boolean isSealed() {
        return isSealed;
    }

    public void setSealed(boolean sealed) {
        isSealed = sealed;
    }

    public List<AccessTimelineEntry> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<AccessTimelineEntry> timeline) {
        this.timeline = timeline;
    }

    public void addTimelineEntry(AccessTimelineEntry entry) {
        if (isSealed) {
            throw new IllegalStateException("Cannot add timeline entries to a sealed access record");
        }
        this.timeline.add(entry);
    }

    public void seal() {
        this.isSealed = true;
    }
}
