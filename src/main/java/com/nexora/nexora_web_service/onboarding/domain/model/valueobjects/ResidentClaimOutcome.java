package com.nexora.nexora_web_service.onboarding.domain.model.valueobjects;

/**
 * Result of a resident credential claim, so the REST layer can return a
 * meaningful message without leaking which step failed.
 */
public enum ResidentClaimOutcome {
    /** Credentials re-issued and emailed to the personal address. */
    SENT,
    /** No building/apartment/resident matched the request. */
    NOT_FOUND,
    /** The apartment was already activated by its resident; it cannot be claimed. */
    ALREADY_ACTIVE
}
