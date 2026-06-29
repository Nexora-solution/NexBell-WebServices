package com.nexora.nexora_web_service.onboarding.interfaces.rest.resources;

import java.util.List;

public record ContractProvisionResultResource(
        Long buildingId,
        String buildingLetter,
        List<CredentialResource> residents,
        List<CredentialResource> doormen
) {
}
