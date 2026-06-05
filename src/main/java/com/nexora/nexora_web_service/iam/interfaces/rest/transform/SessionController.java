package com.nexora.nexora_web_service.iam.interfaces.rest.transform;

import com.nexora.nexora_web_service.iam.domain.model.commands.RevokeSessionCommand;
import com.nexora.nexora_web_service.iam.domain.services.UserSessionCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam")
@Tag(name = "Sessions", description = "Session management endpoints")
public class SessionController {

    private final UserSessionCommandService userSessionCommandService;

    public SessionController(UserSessionCommandService userSessionCommandService) {
        this.userSessionCommandService = userSessionCommandService;
    }

    @PostMapping("/logout")
    @Operation(summary = "Close active session (logout)")
    public ResponseEntity<String> logout(@RequestParam String refreshToken) {
        var command = new RevokeSessionCommand(refreshToken);
        userSessionCommandService.handle(command);
        return ResponseEntity.ok("Session closed successfully");
    }
}
