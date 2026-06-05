package com.nexora.nexora_web_service.iam.domain.services;

import com.nexora.nexora_web_service.iam.domain.model.commands.LoginCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RefreshSessionCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RevokeSessionCommand;

import java.util.Optional;

public interface UserSessionCommandService {
    Optional<TokenPair> handle(LoginCommand command);
    Optional<TokenPair> handle(RefreshSessionCommand command);
    void handle(RevokeSessionCommand command);
}
