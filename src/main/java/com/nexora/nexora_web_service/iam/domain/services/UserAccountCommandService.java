package com.nexora.nexora_web_service.iam.domain.services;

import com.nexora.nexora_web_service.iam.domain.model.commands.*;
import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;

import java.util.Optional;

public interface UserAccountCommandService {
    Optional<UserAccount> handle(RegisterUserCommand command);
    Optional<UserAccount> handle(ChangePasswordCommand command);
    String handle(RequestPasswordResetCommand command);
    boolean handle(ConfirmPasswordResetCommand command);
}
