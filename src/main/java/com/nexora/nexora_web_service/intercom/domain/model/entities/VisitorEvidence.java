package com.nexora.nexora_web_service.intercom.domain.model.entities;

import com.nexora.nexora_web_service.intercom.domain.model.valueobjects.EvidenceUri;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

@Entity
@Table(name = "visitor_evidences")
public class VisitorEvidence extends AuditableModel {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "uri", column = @Column(name = "photo_url", nullable = false))
    })
    private EvidenceUri photoUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "uri", column = @Column(name = "audio_url"))
    })
    private EvidenceUri audioUrl;

    public VisitorEvidence() {}

    public VisitorEvidence(EvidenceUri photoUrl, EvidenceUri audioUrl) {
        this.photoUrl = photoUrl;
        this.audioUrl = audioUrl;
    }

    public EvidenceUri getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(EvidenceUri photoUrl) {
        this.photoUrl = photoUrl;
    }

    public EvidenceUri getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(EvidenceUri audioUrl) {
        this.audioUrl = audioUrl;
    }
}
