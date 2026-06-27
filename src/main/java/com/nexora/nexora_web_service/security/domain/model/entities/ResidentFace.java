package com.nexora.nexora_web_service.security.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Associates an on-device face ID (the integer the ESP32 assigns when a face is
 * enrolled) with a resident. When the ESP32 later recognizes that face ID, the
 * backend uses this to tell the doorman which resident arrived.
 */
@Entity
@Table(name = "resident_faces")
public class ResidentFace extends AuditableModel {

    @Column(name = "face_id", nullable = false, unique = true)
    private Integer faceId;     // ID que el ESP32 le dio a la cara al registrarla

    @Column(name = "resident_id", nullable = false)
    private Long residentId;    // ResidentDirectoryProfile id

    public ResidentFace() {}

    public ResidentFace(Integer faceId, Long residentId) {
        this.faceId = faceId;
        this.residentId = residentId;
    }

    public Integer getFaceId() { return faceId; }
    public void setFaceId(Integer faceId) { this.faceId = faceId; }

    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }
}
