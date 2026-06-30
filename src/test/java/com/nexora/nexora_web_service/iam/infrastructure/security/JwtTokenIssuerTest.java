package com.nexora.nexora_web_service.iam.infrastructure.security;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.PasswordHash;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenIssuerTest {

    private JwtTokenIssuer issuer;

    @BeforeEach
    void setUp() {
        issuer = new JwtTokenIssuer();
        ReflectionTestUtils.setField(issuer, "jwtSecret",
                "test-secret-key-test-secret-key-test-secret-key");
        ReflectionTestUtils.setField(issuer, "jwtExpirationMs", 86_400_000L);
    }

    private UserAccount account() {
        return new UserAccount(new EmailAddress("doorman@nexbell.app"),
                new PasswordHash("hashed"), RoleName.DOORMAN);
    }

    @Test
    void issuedToken_isValidAndCarriesEmailAndRole() {
        String token = issuer.issueAccessToken(account());

        assertTrue(issuer.validate(token));
        assertEquals("doorman@nexbell.app", issuer.getEmailFromToken(token));
        assertEquals("DOORMAN", issuer.getRoleFromToken(token));
    }

    @Test
    void validate_rejectsGarbageToken() {
        assertFalse(issuer.validate("not-a-jwt"));
    }

    @Test
    void validate_rejectsTokenSignedWithAnotherSecret() {
        String token = issuer.issueAccessToken(account());

        JwtTokenIssuer other = new JwtTokenIssuer();
        ReflectionTestUtils.setField(other, "jwtSecret",
                "completely-different-secret-completely-different");
        ReflectionTestUtils.setField(other, "jwtExpirationMs", 86_400_000L);

        assertFalse(other.validate(token));
    }
}
