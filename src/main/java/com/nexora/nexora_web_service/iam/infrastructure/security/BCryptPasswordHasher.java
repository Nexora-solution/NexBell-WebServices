package com.nexora.nexora_web_service.iam.infrastructure.security;

import com.nexora.nexora_web_service.iam.domain.services.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Hashes passwords with BCrypt (random per-password salt baked into the hash).
 * Replaces the former plaintext hasher. The onboarding credential email is sent
 * with the plaintext password BEFORE it reaches here, so the doorman/resident
 * claim flow is unaffected — only what gets persisted changes.
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    @Override
    public boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null) {
            return false;
        }
        return encoder.matches(plainPassword, hash);
    }
}
