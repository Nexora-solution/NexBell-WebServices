package com.nexora.nexora_web_service.directory.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "doorman_buildings")
public class DoormanBuilding extends AuditableModel {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    // System login email of the doorman (e.g. doorman1_a@nexbell.app). Stored
    // here so credentials can be re-issued without an extra IAM lookup.
    @Column(name = "login_email")
    private String loginEmail;

    // Personal/real email the doorman gave at contract time — the channel where
    // generated credentials are delivered and the key used to reclaim them.
    @Column(name = "personal_email")
    private String personalEmail;

    // Doorman profile: name defaults to "Portero" until they edit it; phone is
    // empty until entered; photo is an optional base64 data URL they upload.
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    public DoormanBuilding() {}

    public DoormanBuilding(Long userId, Long buildingId) {
        this.userId = userId;
        this.buildingId = buildingId;
    }

    public Long getUserId() { return userId; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

    public String getLoginEmail() { return loginEmail; }
    public void setLoginEmail(String loginEmail) { this.loginEmail = loginEmail; }

    public String getPersonalEmail() { return personalEmail; }
    public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
