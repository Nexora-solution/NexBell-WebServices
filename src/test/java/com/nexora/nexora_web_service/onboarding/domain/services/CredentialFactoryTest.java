package com.nexora.nexora_web_service.onboarding.domain.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialFactoryTest {

    @Test
    void buildingLetter_isSpreadsheetStyle() {
        assertEquals("a", CredentialFactory.buildingLetter(1));
        assertEquals("z", CredentialFactory.buildingLetter(26));
        assertEquals("aa", CredentialFactory.buildingLetter(27));
    }

    @Test
    void emails_areBuildingScopedSoTheyNeverCollide() {
        assertEquals("resident1_a@nexbell.app", CredentialFactory.residentEmail(1, "a"));
        assertEquals("resident1_b@nexbell.app", CredentialFactory.residentEmail(1, "b"));
        assertEquals("doorman1_a@nexbell.app", CredentialFactory.doormanEmail(1, "a"));
    }

    @Test
    void passwords_haveExpectedShape() {
        assertTrue(CredentialFactory.residentPassword().matches("resident!\\d{2}"));
        assertTrue(CredentialFactory.doormanPassword().matches("doorman!\\d{2}"));
    }
}
