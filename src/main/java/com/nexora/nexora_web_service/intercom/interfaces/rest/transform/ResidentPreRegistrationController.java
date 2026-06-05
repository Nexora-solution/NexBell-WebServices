package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.intercom.domain.model.commands.CancelPreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.CreatePreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.UpdatePreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.entities.PreRegisteredVisit;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.CreatePreRegisteredVisitResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intercom/pre-registered-visits")
@Tag(name = "Pre-registered Visits", description = "Management of pre-authorized expected visitors")
public class ResidentPreRegistrationController {

    private final IntercomCommandService commandService;

    public ResidentPreRegistrationController(IntercomCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    @Operation(summary = "Register a new expected pre-registered visit")
    public ResponseEntity<PreRegisteredVisit> createPreRegisteredVisit(@Valid @RequestBody CreatePreRegisteredVisitResource resource) {
        var command = new CreatePreRegisteredVisitCommand(
                resource.residentId(),
                resource.visitorName(),
                resource.visitorDocument(),
                resource.expectedAt()
        );
        var visitOpt = commandService.handle(command);
        return visitOpt.map(visit -> ResponseEntity.status(HttpStatus.CREATED).body(visit))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update details of an active pre-registered visit")
    public ResponseEntity<PreRegisteredVisit> updatePreRegisteredVisit(@PathVariable Long id, @Valid @RequestBody CreatePreRegisteredVisitResource resource) {
        var command = new UpdatePreRegisteredVisitCommand(
                id,
                resource.visitorName(),
                resource.visitorDocument(),
                resource.expectedAt()
        );
        var visitOpt = commandService.handle(command);
        return visitOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel / invalidate an active pre-registered visit")
    public ResponseEntity<String> cancelPreRegisteredVisit(@PathVariable Long id) {
        var command = new CancelPreRegisteredVisitCommand(id);
        commandService.handle(command);
        return ResponseEntity.ok("Pre-registered visit canceled successfully");
    }
}
