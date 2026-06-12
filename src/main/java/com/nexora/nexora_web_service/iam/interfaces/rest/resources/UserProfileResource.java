package com.nexora.nexora_web_service.iam.interfaces.rest.resources;

public record UserProfileResource(
        Long id,
        String email,
        String fullName,
        String role,
        String avatar,
        Long apartmentId
) {}
