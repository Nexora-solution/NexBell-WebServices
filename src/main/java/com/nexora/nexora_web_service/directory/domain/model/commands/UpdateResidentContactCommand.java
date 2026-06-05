package com.nexora.nexora_web_service.directory.domain.model.commands;

import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ContactChannel;

public record UpdateResidentContactCommand(Long residentId, ContactChannel contact) {
    public UpdateResidentContactCommand {
        if (residentId == null || residentId <= 0) throw new IllegalArgumentException("Resident ID must be positive");
        if (contact == null) throw new IllegalArgumentException("Contact channel cannot be null");
    }
}
