package com.nexora.nexora_web_service.onboarding.domain.model.commands;

/**
 * A resident reclaims their credentials from the mobile app by giving the
 * building they live in, their apartment code and the personal email where
 * they want the credentials delivered. Mirrors the doorman claim, but the
 * personal email is purely a delivery address (it is not stored or matched).
 *
 * Ownership is NOT proven by the email: the only gate is that the apartment
 * must still be PENDING (its resident has never activated it). Once a resident
 * activates the apartment on first login (sets name + phone), it can no longer
 * be claimed.
 */
public record ClaimResidentCredentialsCommand(String buildingName, String apartmentCode, String personalEmail) {
    public ClaimResidentCredentialsCommand {
        if (buildingName == null || buildingName.isBlank()) throw new IllegalArgumentException("Building name cannot be empty");
        if (apartmentCode == null || apartmentCode.isBlank()) throw new IllegalArgumentException("Apartment code cannot be empty");
        if (personalEmail == null || personalEmail.isBlank()) throw new IllegalArgumentException("Personal email cannot be empty");
    }
}
