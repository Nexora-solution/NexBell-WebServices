package com.nexora.nexora_web_service.iam.application.internal.queryservices;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserSession;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserSessionByRefreshTokenQuery;
import com.nexora.nexora_web_service.iam.domain.services.UserSessionQueryService;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserSessionQueryServiceImpl implements UserSessionQueryService {

    private final UserSessionRepository userSessionRepository;

    public UserSessionQueryServiceImpl(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    public Optional<UserSession> handle(GetUserSessionByRefreshTokenQuery query) {
        return userSessionRepository.findByRefreshToken(query.token());
    }
}
