package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.intercom.domain.model.commands.RegisterAccessDecisionCommand;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.RegisterDecisionResource;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.VisitRequestDetailResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intercom")
@Tag(name = "Resident Decisions", description = "Endpoints for resident approval or rejection of visits")
public class ResidentDecisionController {

    private final IntercomCommandService commandService;

    public ResidentDecisionController(IntercomCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/visit-requests/{id}/decision")
    @Operation(summary = "Register the resident's decision (APPROVED / REJECTED) for a visit request")
    public ResponseEntity<VisitRequestDetailResource> registerDecision(@PathVariable Long id, @Valid @RequestBody RegisterDecisionResource resource) {
        var command = new RegisterAccessDecisionCommand(id, resource.decision());
        var requestOpt = commandService.handle(command);
        return requestOpt.map(req -> ResponseEntity.ok(toDetailResource(req)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    private VisitRequestDetailResource toDetailResource(VisitRequest req) {
        String photoUrl = req.getEvidence() != null ? req.getEvidence().getPhotoUrl().uri() : null;
        String audioUrl = req.getEvidence() != null && req.getEvidence().getAudioUrl() != null ? req.getEvidence().getAudioUrl().uri() : null;
        return new VisitRequestDetailResource(
                req.getId(),
                req.getVisitorName(),
                req.getApartmentId(),
                req.getStatus(),
                photoUrl,
                audioUrl,
                req.getCreatedAt()
        );
    }
}
