package com.nexora.nexora_web_service.onboarding.domain.model.valueobjects;

import java.util.List;

/** Outcome of provisioning a contract: the new building plus every credential created. */
public record ContractProvisionResult(
        Long buildingId,
        String buildingLetter,
        List<GeneratedCredential> residents,
        List<GeneratedCredential> doormen
) {
}
