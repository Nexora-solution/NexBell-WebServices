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

    public DoormanBuilding() {}

    public DoormanBuilding(Long userId, Long buildingId) {
        this.userId = userId;
        this.buildingId = buildingId;
    }

    public Long getUserId() { return userId; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
}
