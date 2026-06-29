package com.nexora.nexora_web_service.onboarding.domain.model.valueobjects;

/**
 * A single generated login, shown once on the confirmation screen.
 * personalEmail is only set for doormen (where credentials are also emailed).
 */
public record GeneratedCredential(String label, String email, String password, String personalEmail) {
}
