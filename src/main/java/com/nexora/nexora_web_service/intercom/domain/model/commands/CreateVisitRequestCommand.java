package com.nexora.nexora_web_service.intercom.domain.model.commands;

public record CreateVisitRequestCommand(String visitorName, Long apartmentId) {
    public CreateVisitRequestCommand {
        if (visitorName == null || visitorName.isBlank()) throw new IllegalArgumentException("Visitor name cannot be empty");
        if (apartmentId == null || apartmentId <= 0) throw new IllegalArgumentException("Apartment ID must be positive");
    }
}
