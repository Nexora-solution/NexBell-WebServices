package com.nexora.nexora_web_service.security.domain.model.commands;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;

public record EvaluateAccessCommand(RoleName role, String permissionCode) {
    public EvaluateAccessCommand {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        if (permissionCode == null || permissionCode.isBlank()) throw new IllegalArgumentException("Permission code cannot be empty");
    }
}
