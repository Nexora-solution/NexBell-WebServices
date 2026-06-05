package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.intercom.domain.model.commands.AttachVisitorEvidenceCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.CreateVisitRequestCommand;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.AttachEvidenceResource;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.CreateVisitRequestResource;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.VisitRequestDetailResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("intercomIoTIngressController")
@RequestMapping("/api/intercom")
@Tag(name = "IoT Ingress (Intercom)", description = "IoT hardware door intercom ingress endpoints")
public class IoTIngressController {

    private final IntercomCommandService commandService;

    public IoTIngressController(IntercomCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/visit-requests")
    @Operation(summary = "IoT device signals a visitor has pressed the doorbell and creates a request")
    public ResponseEntity<VisitRequestDetailResource> createVisitRequest(@Valid @RequestBody CreateVisitRequestResource resource) {
        var command = new CreateVisitRequestCommand(resource.visitorName(), resource.apartmentId());
        var requestOpt = commandService.handle(command);
        return requestOpt.map(req -> ResponseEntity.status(HttpStatus.CREATED).body(toDetailResource(req)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/visit-requests/{id}/evidence")
    @Operation(summary = "IoT device uploads visitor photo and optional audio message evidence")
    public ResponseEntity<VisitRequestDetailResource> attachEvidence(@PathVariable Long id, @Valid @RequestBody AttachEvidenceResource resource) {
        var command = new AttachVisitorEvidenceCommand(id, resource.photoUrl(), resource.audioUrl());
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
