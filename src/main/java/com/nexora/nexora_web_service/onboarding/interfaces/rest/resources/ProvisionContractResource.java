package com.nexora.nexora_web_service.onboarding.interfaces.rest.resources;

import java.util.List;

public record ProvisionContractResource(
        String buildingName,
        String address,
        String district,
        String imageUrl,
        Integer floors,
        Integer apartments,
        Integer doormen,
        List<String> doormanPersonalEmails,
        String phone
) {
}
