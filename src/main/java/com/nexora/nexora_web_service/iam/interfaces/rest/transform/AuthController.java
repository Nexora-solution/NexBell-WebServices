package com.nexora.nexora_web_service.iam.interfaces.rest.transform;

import com.nexora.nexora_web_service.iam.domain.model.commands.LoginCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RefreshSessionCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RegisterUserCommand;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.services.TokenIssuer;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountCommandService;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountQueryService;
import com.nexora.nexora_web_service.iam.domain.services.UserSessionCommandService;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.LoginResource;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.RefreshSessionResource;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.RegisterUserResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iam")
@Tag(name = "Authentication", description = "Authentication & registration endpoints")
public class AuthController {

    private final UserAccountCommandService userAccountCommandService;
    private final UserAccountQueryService userAccountQueryService;
    private final UserSessionCommandService userSessionCommandService;
    private final TokenIssuer tokenIssuer;

    public AuthController(UserAccountCommandService userAccountCommandService,
                          UserAccountQueryService userAccountQueryService,
                          UserSessionCommandService userSessionCommandService,
                          TokenIssuer tokenIssuer) {
        this.userAccountCommandService = userAccountCommandService;
        this.userAccountQueryService = userAccountQueryService;
        this.userSessionCommandService = userSessionCommandService;
        this.tokenIssuer = tokenIssuer;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterUserResource resource) {
        var command = new RegisterUserCommand(resource.email(), resource.password(), resource.role());
        var accountOpt = userAccountCommandService.handle(command);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to register user");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and open session")
    public ResponseEntity<AuthenticatedUserResource> login(@Valid @RequestBody LoginResource resource) {
        var command = new LoginCommand(resource.email(), resource.password());
        var tokenPairOpt = userSessionCommandService.handle(command);
        if (tokenPairOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var tokens = tokenPairOpt.get();
        var account = userAccountQueryService.handle(new GetUserAccountByEmailQuery(new EmailAddress(resource.email())))
                .orElseThrow(() -> new IllegalStateException("User account not found after successful login"));

        var response = new AuthenticatedUserResource(
                account.getId(),
                account.getEmail().email(),
                tokens.accessToken(),
                tokens.refreshToken()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using active refresh token")
    public ResponseEntity<AuthenticatedUserResource> refresh(@Valid @RequestBody RefreshSessionResource resource) {
        var command = new RefreshSessionCommand(resource.refreshToken());
        var tokenPairOpt = userSessionCommandService.handle(command);
        if (tokenPairOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var tokens = tokenPairOpt.get();
        String email = tokenIssuer.getEmailFromToken(tokens.accessToken());
        var account = userAccountQueryService.handle(new GetUserAccountByEmailQuery(new EmailAddress(email)))
                .orElseThrow(() -> new IllegalStateException("User account not found after session refresh"));

        var response = new AuthenticatedUserResource(
                account.getId(),
                account.getEmail().email(),
                tokens.accessToken(),
                tokens.refreshToken()
        );
        return ResponseEntity.ok(response);
    }
}
