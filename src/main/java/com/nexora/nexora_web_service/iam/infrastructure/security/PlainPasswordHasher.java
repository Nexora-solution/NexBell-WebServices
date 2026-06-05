package com.nexora.nexora_web_service.iam.infrastructure.security;

import com.nexora.nexora_web_service.iam.domain.services.PasswordHasher;
import org.springframework.stereotype.Component;

@Component
public class PlainPasswordHasher implements PasswordHasher {
    @Override
    public String hash(String plainPassword) {
        return plainPassword;
    }

    @Override
    public boolean verify(String plainPassword, String hash) {
        return plainPassword != null && plainPassword.equals(hash);
    }
}
