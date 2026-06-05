package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.security.domain.model.commands.EvaluateAccessCommand;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.AuthorizeResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
@Tag(name = "Authorization", description = "Role functional permissions check")
public class AuthorizationController {

    private final SecurityCommandService securityCommandService;

    public AuthorizationController(SecurityCommandService securityCommandService) {
        this.securityCommandService = securityCommandService;
    }

    @PostMapping("/authorize")
    @Operation(summary = "Evaluate if a role has access to execute a permission")
    public ResponseEntity<Boolean> authorize(@Valid @RequestBody AuthorizeResource resource) {
        var command = new EvaluateAccessCommand(resource.role(), resource.permissionCode());
        boolean hasAccess = securityCommandService.handle(command);
        return ResponseEntity.ok(hasAccess);
    }
}
