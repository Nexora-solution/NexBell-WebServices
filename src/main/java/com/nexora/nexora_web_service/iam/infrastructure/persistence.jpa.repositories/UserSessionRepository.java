package com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserSession;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RefreshTokenValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshToken(RefreshTokenValue refreshToken);
    List<UserSession> findByUserAccountIdAndIsRevokedFalse(Long userAccountId);
}
