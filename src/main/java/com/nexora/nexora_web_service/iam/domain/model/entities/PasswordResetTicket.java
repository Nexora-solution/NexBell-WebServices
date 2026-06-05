package com.nexora.nexora_web_service.iam.domain.model.entities;

import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tickets")
public class PasswordResetTicket extends AuditableModel {

    @Column(name = "user_account_id", nullable = false)
    private Long userAccountId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isUsed = false;

    public PasswordResetTicket() {}

    public PasswordResetTicket(Long userAccountId, String token, LocalDateTime expiresAt) {
        this.userAccountId = userAccountId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.isUsed = false;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiresAt);
    }

    public void markAsUsed() {
        this.isUsed = true;
    }
}
