package com.nexora.nexora_web_service.iam.domain.model.entities;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RefreshTokenValue;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession extends AuditableModel {

    @Column(name = "user_account_id", nullable = false)
    private Long userAccountId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "token", column = @Column(name = "refresh_token", nullable = false))
    })
    private RefreshTokenValue refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isRevoked = false;

    public UserSession() {}

    public UserSession(Long userAccountId, RefreshTokenValue refreshToken, LocalDateTime expiresAt) {
        this.userAccountId = userAccountId;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.isRevoked = false;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public RefreshTokenValue getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(RefreshTokenValue refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void revoke() {
        this.isRevoked = true;
    }

    public void rotateRefreshToken(RefreshTokenValue newToken, LocalDateTime newExpiresAt) {
        this.refreshToken = newToken;
        this.expiresAt = newExpiresAt;
    }
}
