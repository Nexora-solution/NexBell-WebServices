package com.nexora.nexora_web_service.iam.domain.services;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserSession;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserSessionByRefreshTokenQuery;

import java.util.Optional;

public interface UserSessionQueryService {
    Optional<UserSession> handle(GetUserSessionByRefreshTokenQuery query);
}
