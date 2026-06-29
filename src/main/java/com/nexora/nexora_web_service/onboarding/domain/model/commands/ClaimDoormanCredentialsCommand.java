package com.nexora.nexora_web_service.onboarding.domain.model.commands;

/**
 * A doorman who never received (or lost) their credentials reclaims them by
 * giving the building they belong to and their personal email. The system
 * re-issues a fresh password and emails it to that personal address.
 */
public record ClaimDoormanCredentialsCommand(Long buildingId, String personalEmail) {
    public ClaimDoormanCredentialsCommand {
        if (buildingId == null || buildingId <= 0) throw new IllegalArgumentException("Building ID must be positive");
        if (personalEmail == null || personalEmail.isBlank()) throw new IllegalArgumentException("Personal email cannot be empty");
    }
}
