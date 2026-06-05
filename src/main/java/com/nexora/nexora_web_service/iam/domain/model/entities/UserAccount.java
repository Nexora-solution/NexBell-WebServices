package com.nexora.nexora_web_service.iam.domain.model.entities;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.PasswordHash;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

@Entity
@Table(name = "user_accounts")
public class UserAccount extends AuditableModel {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "email", column = @Column(name = "email", nullable = false, unique = true))
    })
    private EmailAddress email;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "passwordHash", column = @Column(name = "password_hash", nullable = false))
    })
    private PasswordHash password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleName role;

    @Column(nullable = false)
    private boolean isActive = true;

    public UserAccount() {
        this.isActive = true;
    }

    public UserAccount(EmailAddress email, PasswordHash password, RoleName role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = true;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public void setEmail(EmailAddress email) {
        this.email = email;
    }

    public PasswordHash getPassword() {
        return password;
    }

    public void setPassword(PasswordHash password) {
        this.password = password;
    }

    public RoleName getRole() {
        return role;
    }

    public void setRole(RoleName role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void updatePassword(PasswordHash newPassword) {
        this.password = newPassword;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
