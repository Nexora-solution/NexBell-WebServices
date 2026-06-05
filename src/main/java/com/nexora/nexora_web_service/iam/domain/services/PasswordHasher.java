package com.nexora.nexora_web_service.iam.domain.services;

public interface PasswordHasher {
    String hash(String plainPassword);
    boolean verify(String plainPassword, String hash);
}
