package com.nexora.nexora_web_service.directory.domain.model.entities;

import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ApartmentCode;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

@Entity
@Table(name = "apartments")
public class Apartment extends AuditableModel {

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "code", column = @Column(name = "code", nullable = false))
    })
    private ApartmentCode code;

    @Column(name = "resident_id")
    private Long residentId;

    public Apartment() {}

    public Apartment(Long buildingId, ApartmentCode code) {
        this.buildingId = buildingId;
        this.code = code;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public ApartmentCode getCode() {
        return code;
    }

    public void setCode(ApartmentCode code) {
        this.code = code;
    }

    public Long getResidentId() {
        return residentId;
    }

    public void setResidentId(Long residentId) {
        this.residentId = residentId;
    }

    public void assignResident(Long residentId) {
        this.residentId = residentId;
    }

    public void unassignResident() {
        this.residentId = null;
    }

    public void updateLabel(ApartmentCode newCode) {
        this.code = newCode;
    }
}
