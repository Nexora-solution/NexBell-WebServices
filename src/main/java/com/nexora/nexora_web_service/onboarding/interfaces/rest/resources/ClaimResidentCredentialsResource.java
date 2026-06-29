package com.nexora.nexora_web_service.onboarding.interfaces.rest.resources;

/** Body for a resident reclaiming credentials from the mobile app. */
public record ClaimResidentCredentialsResource(String buildingName, String apartmentCode, String personalEmail) {
}
