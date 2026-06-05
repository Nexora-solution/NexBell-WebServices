package com.nexora.nexora_web_service.iam.application.internal.commandservices;

import com.nexora.nexora_web_service.iam.domain.model.commands.LoginCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RefreshSessionCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RevokeSessionCommand;
import com.nexora.nexora_web_service.iam.domain.model.entities.UserSession;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RefreshTokenValue;
import com.nexora.nexora_web_service.iam.domain.services.PasswordHasher;
import com.nexora.nexora_web_service.iam.domain.services.TokenIssuer;
import com.nexora.nexora_web_service.iam.domain.services.TokenPair;
import com.nexora.nexora_web_service.iam.domain.services.UserSessionCommandService;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.UserAccountRepository;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserSessionCommandServiceImpl implements UserSessionCommandService {

    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public UserSessionCommandServiceImpl(UserAccountRepository userAccountRepository,
                                         UserSessionRepository userSessionRepository,
                                         PasswordHasher passwordHasher,
                                         TokenIssuer tokenIssuer) {
        this.userAccountRepository = userAccountRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Optional<TokenPair> handle(LoginCommand command) {
        var email = new EmailAddress(command.email());
        var account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!account.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        if (!passwordHasher.verify(command.password(), account.getPassword().passwordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        var accessToken = tokenIssuer.issueAccessToken(account);

        var refreshTokenStr = UUID.randomUUID().toString();
        var expiresAt = LocalDateTime.now().plusDays(7);
        var session = new UserSession(account.getId(), new RefreshTokenValue(refreshTokenStr), expiresAt);
        userSessionRepository.save(session);

        return Optional.of(new TokenPair(accessToken, refreshTokenStr));
    }

    @Override
    public Optional<TokenPair> handle(RefreshSessionCommand command) {
        var tokenVal = new RefreshTokenValue(command.refreshToken());
        var session = userSessionRepository.findByRefreshToken(tokenVal)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (session.isRevoked() || session.isExpired()) {
            throw new IllegalArgumentException("Refresh token has expired or been revoked");
        }

        var account = userAccountRepository.findById(session.getUserAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        var newAccessToken = tokenIssuer.issueAccessToken(account);

        var newRefreshTokenStr = UUID.randomUUID().toString();
        var newExpiresAt = LocalDateTime.now().plusDays(7);
        session.rotateRefreshToken(new RefreshTokenValue(newRefreshTokenStr), newExpiresAt);
        userSessionRepository.save(session);

        return Optional.of(new TokenPair(newAccessToken, newRefreshTokenStr));
    }

    @Override
    public void handle(RevokeSessionCommand command) {
        var tokenVal = new RefreshTokenValue(command.token());
        userSessionRepository.findByRefreshToken(tokenVal).ifPresent(session -> {
            session.revoke();
            userSessionRepository.save(session);
        });
    }
}
