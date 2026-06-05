package com.nexora.nexora_web_service.iam.domain.model.events;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;

import java.time.Instant;

public class UserRegisteredEvent {
    private final Long userId;
    private final String email;
    private final RoleName role;
    private final Instant occurredAt;

    public UserRegisteredEvent(Long userId, String email, RoleName role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.occurredAt = Instant.now();
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public RoleName getRole() { return role; }
    public Instant getOccurredAt() { return occurredAt; }
}
