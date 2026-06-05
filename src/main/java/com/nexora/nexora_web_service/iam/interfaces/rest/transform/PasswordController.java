package com.nexora.nexora_web_service.iam.interfaces.rest.transform;

import com.nexora.nexora_web_service.iam.domain.model.commands.ChangePasswordCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.ConfirmPasswordResetCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountCommandService;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.ChangePasswordResource;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.ConfirmPasswordResetResource;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.RequestPasswordResetResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam/password")
@Tag(name = "Passwords", description = "Password management and recovery endpoints")
public class PasswordController {

    private final UserAccountCommandService userAccountCommandService;

    public PasswordController(UserAccountCommandService userAccountCommandService) {
        this.userAccountCommandService = userAccountCommandService;
    }

    @PostMapping("/request-reset")
    @Operation(summary = "Request a password reset link/ticket")
    public ResponseEntity<String> requestReset(@Valid @RequestBody RequestPasswordResetResource resource) {
        var command = new RequestPasswordResetCommand(resource.email());
        var token = userAccountCommandService.handle(command);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/confirm-reset")
    @Operation(summary = "Confirm password reset using token")
    public ResponseEntity<String> confirmReset(@Valid @RequestBody ConfirmPasswordResetResource resource) {
        var command = new ConfirmPasswordResetCommand(resource.token(), resource.newPassword());
        boolean success = userAccountCommandService.handle(command);
        if (success) {
            return ResponseEntity.ok("Password reset successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to reset password");
        }
    }

    @PutMapping("/change")
    @Operation(summary = "Change password from user profile")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordResource resource) {
        var command = new ChangePasswordCommand(resource.email(), resource.oldPassword(), resource.newPassword());
        userAccountCommandService.handle(command);
        return ResponseEntity.ok("Password changed successfully");
    }
}
