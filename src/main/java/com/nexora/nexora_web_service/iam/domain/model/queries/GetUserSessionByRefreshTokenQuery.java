package com.nexora.nexora_web_service.iam.domain.model.queries;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RefreshTokenValue;

public record GetUserSessionByRefreshTokenQuery(RefreshTokenValue token) {
    public GetUserSessionByRefreshTokenQuery {
        if (token == null) throw new IllegalArgumentException("Token cannot be null");
    }
}
