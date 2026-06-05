package com.nexora.nexora_web_service.directory.domain.model.commands;

public record CreateApartmentCommand(Long buildingId, String code) {
    public CreateApartmentCommand {
        if (buildingId == null || buildingId <= 0) throw new IllegalArgumentException("Building ID must be positive");
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Apartment code cannot be empty");
    }
}
