package com.nexora.nexora_web_service.directory.domain.model.commands;

import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ContactChannel;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ResidentDocument;

public record AssignResidentCommand(
    Long apartmentId,
    Long userId,
    String fullName,
    ResidentDocument document,
    ContactChannel contact
) {
    public AssignResidentCommand {
        if (apartmentId == null || apartmentId <= 0) throw new IllegalArgumentException("Apartment ID must be positive");
        if (userId == null || userId <= 0) throw new IllegalArgumentException("User ID must be positive");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Full name cannot be empty");
        if (document == null) throw new IllegalArgumentException("Document cannot be null");
        if (contact == null) throw new IllegalArgumentException("Contact cannot be null");
    }
}
