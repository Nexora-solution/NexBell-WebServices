package com.nexora.nexora_web_service.iam.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hash_doesNotReturnPlaintext() {
        String hash = hasher.hash("S3cret!");
        assertNotEquals("S3cret!", hash);
        assertTrue(hash.startsWith("$2"), "should be a BCrypt hash");
    }

    @Test
    void verify_matchesCorrectPassword() {
        String hash = hasher.hash("S3cret!");
        assertTrue(hasher.verify("S3cret!", hash));
    }

    @Test
    void verify_rejectsWrongPassword() {
        String hash = hasher.hash("S3cret!");
        assertFalse(hasher.verify("wrong", hash));
    }

    @Test
    void verify_handlesNullsSafely() {
        assertFalse(hasher.verify(null, "x"));
        assertFalse(hasher.verify("x", null));
    }
}
