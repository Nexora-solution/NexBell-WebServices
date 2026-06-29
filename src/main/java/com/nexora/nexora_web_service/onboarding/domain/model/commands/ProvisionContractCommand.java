package com.nexora.nexora_web_service.onboarding.domain.model.commands;

import java.util.List;

/**
 * One contract = one fully provisioned building. The onboarding context creates
 * the building, its apartments + resident accounts and the doorman accounts in a
 * single transactional use case, with building-scoped identities so two contracts
 * never collide.
 */
public record ProvisionContractCommand(
        String buildingName,
        String address,
        String district,
        String imageUrl,
        int floors,
        int apartments,
        int doormen,
        List<String> doormanPersonalEmails,
        String phone
) {
    public ProvisionContractCommand {
        if ((buildingName == null || buildingName.isBlank()) && (address == null || address.isBlank()))
            throw new IllegalArgumentException("A building needs at least a name or an address");
        if (apartments < 0) throw new IllegalArgumentException("Apartments cannot be negative");
        if (doormen < 0) throw new IllegalArgumentException("Doormen cannot be negative");
        if (floors < 1) floors = 1;
        doormanPersonalEmails = doormanPersonalEmails == null ? List.of() : List.copyOf(doormanPersonalEmails);
    }
}
