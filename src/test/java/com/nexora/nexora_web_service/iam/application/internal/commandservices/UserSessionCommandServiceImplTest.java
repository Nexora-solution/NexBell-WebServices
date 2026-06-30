package com.nexora.nexora_web_service.iam.application.internal.commandservices;

import com.nexora.nexora_web_service.iam.domain.model.commands.LoginCommand;
import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.PasswordHash;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.iam.domain.services.PasswordHasher;
import com.nexora.nexora_web_service.iam.domain.services.TokenIssuer;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.UserAccountRepository;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.UserSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSessionCommandServiceImplTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock UserSessionRepository userSessionRepository;
    @Mock PasswordHasher passwordHasher;
    @Mock TokenIssuer tokenIssuer;

    @InjectMocks UserSessionCommandServiceImpl service;

    private UserAccount activeAccount() {
        return new UserAccount(new EmailAddress("user@nexbell.app"),
                new PasswordHash("stored-hash"), RoleName.RESIDENT);
    }

    @Test
    void login_withValidCredentials_returnsTokenPairAndStoresSession() {
        var account = activeAccount();
        when(userAccountRepository.findByEmail(any())).thenReturn(Optional.of(account));
        when(passwordHasher.verify("plain", "stored-hash")).thenReturn(true);
        when(tokenIssuer.issueAccessToken(account)).thenReturn("access-token");

        var result = service.handle(new LoginCommand("user@nexbell.app", "plain"));

        assertTrue(result.isPresent());
        assertEquals("access-token", result.get().accessToken());
        verify(userSessionRepository).save(any());
    }

    @Test
    void login_withWrongPassword_throws() {
        var account = activeAccount();
        when(userAccountRepository.findByEmail(any())).thenReturn(Optional.of(account));
        when(passwordHasher.verify("bad", "stored-hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.handle(new LoginCommand("user@nexbell.app", "bad")));
    }

    @Test
    void login_withUnknownEmail_throws() {
        when(userAccountRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.handle(new LoginCommand("ghost@nexbell.app", "x")));
    }
}
