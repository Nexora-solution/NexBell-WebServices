package com.nexora.nexora_web_service.directory.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "building_directories")
public class BuildingDirectory extends AuditableModel {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    public BuildingDirectory() {}

    public BuildingDirectory(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
