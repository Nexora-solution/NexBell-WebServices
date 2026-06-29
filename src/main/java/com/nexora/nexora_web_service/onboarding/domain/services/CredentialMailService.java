package com.nexora.nexora_web_service.onboarding.domain.services;

/** Delivers generated/re-issued credentials to a doorman's personal email. */
public interface CredentialMailService {
    void sendDoormanCredentials(String toPersonalEmail, String loginEmail, String password, String buildingName);
}
