package com.nexora.nexora_web_service.iam.application.internal.commandservices;

import com.nexora.nexora_web_service.iam.domain.model.commands.*;
import com.nexora.nexora_web_service.iam.domain.model.entities.PasswordResetTicket;
import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.model.events.UserRegisteredEvent;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.PasswordHash;
import com.nexora.nexora_web_service.iam.domain.services.PasswordHasher;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountCommandService;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.PasswordResetTicketRepository;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.UserAccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserAccountCommandServiceImpl implements UserAccountCommandService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetTicketRepository ticketRepository;
    private final PasswordHasher passwordHasher;
    private final ApplicationEventPublisher eventPublisher;

    public UserAccountCommandServiceImpl(UserAccountRepository userAccountRepository,
                                         PasswordResetTicketRepository ticketRepository,
                                         PasswordHasher passwordHasher,
                                         ApplicationEventPublisher eventPublisher) {
        this.userAccountRepository = userAccountRepository;
        this.ticketRepository = ticketRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<UserAccount> handle(RegisterUserCommand command) {
        var email = new EmailAddress(command.email());
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        var passwordHash = new PasswordHash(passwordHasher.hash(command.password()));
        var account = new UserAccount(email, passwordHash, command.role());
        var savedAccount = userAccountRepository.save(account);

        eventPublisher.publishEvent(new UserRegisteredEvent(savedAccount.getId(), savedAccount.getEmail().email(), savedAccount.getRole()));

        return Optional.of(savedAccount);
    }

    @Override
    public Optional<UserAccount> handle(ChangePasswordCommand command) {
        var email = new EmailAddress(command.email());
        var account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordHasher.verify(command.oldPassword(), account.getPassword().passwordHash())) {
            throw new IllegalArgumentException("Incorrect old password");
        }
        var newPasswordHash = new PasswordHash(passwordHasher.hash(command.newPassword()));
        account.updatePassword(newPasswordHash);
        return Optional.of(userAccountRepository.save(account));
    }

    @Override
    public String handle(RequestPasswordResetCommand command) {
        var email = new EmailAddress(command.email());
        var account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var token = UUID.randomUUID().toString();
        var expiresAt = LocalDateTime.now().plusHours(2);
        var ticket = new PasswordResetTicket(account.getId(), token, expiresAt);
        ticketRepository.save(ticket);
        return token;
    }

    @Override
    public boolean handle(ConfirmPasswordResetCommand command) {
        var ticket = ticketRepository.findByToken(command.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));
        if (!ticket.isValid()) {
            throw new IllegalArgumentException("Token has expired or already been used");
        }
        var account = userAccountRepository.findById(ticket.getUserAccountId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var newPasswordHash = new PasswordHash(passwordHasher.hash(command.newPassword()));
        account.updatePassword(newPasswordHash);
        userAccountRepository.save(account);
        ticket.markAsUsed();
        ticketRepository.save(ticket);
        return true;
    }
}
