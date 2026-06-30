package com.nexora.nexora_web_service.iam.infrastructure.security;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.services.TokenIssuer;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    @Value("${authorization.jwt.secret}")
    private String jwtSecret;

    @Value("${authorization.jwt.expiration-milliseconds}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String issueAccessToken(UserAccount account) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(account.getEmail().email())
                .claim("role", account.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validate(String jwt) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getEmailFromToken(String jwt) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }

    @Override
    public String getRoleFromToken(String jwt) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .get("role", String.class);
    }
}
