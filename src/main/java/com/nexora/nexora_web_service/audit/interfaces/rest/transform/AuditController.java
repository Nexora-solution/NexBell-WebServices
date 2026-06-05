package com.nexora.nexora_web_service.audit.interfaces.rest.transform;

import com.nexora.nexora_web_service.audit.domain.model.commands.RegisterAccessRecordCommand;
import com.nexora.nexora_web_service.audit.domain.model.entities.AccessRecord;
import com.nexora.nexora_web_service.audit.domain.model.queries.GetAccessRecordByIdQuery;
import com.nexora.nexora_web_service.audit.domain.services.AuditCommandService;
import com.nexora.nexora_web_service.audit.domain.services.AuditQueryService;
import com.nexora.nexora_web_service.audit.interfaces.rest.resources.AccessRecordResource;
import com.nexora.nexora_web_service.audit.interfaces.rest.resources.RegisterAccessRecordResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Access Records Audit", description = "Endpoints for registering and querying immutable access logs")
public class AuditController {

    private final AuditCommandService commandService;
    private final AuditQueryService queryService;

    public AuditController(AuditCommandService commandService, AuditQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/access-records")
    @Operation(summary = "Explicitly register a new sealed access record in the log")
    public ResponseEntity<AccessRecordResource> registerAccessRecord(@Valid @RequestBody RegisterAccessRecordResource resource) {
        var command = new RegisterAccessRecordCommand(resource.correlationId(), resource.decision());
        var recordOpt = commandService.handle(command);
        return recordOpt.map(record -> ResponseEntity.status(HttpStatus.CREATED).body(toResource(record)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/access-records/{id}")
    @Operation(summary = "Get a detailed access record by its ID")
    public ResponseEntity<AccessRecordResource> getAccessRecordById(@PathVariable Long id) {
        var query = new GetAccessRecordByIdQuery(id);
        var recordOpt = queryService.handle(query);
        return recordOpt.map(record -> ResponseEntity.ok(toResource(record)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private AccessRecordResource toResource(AccessRecord record) {
        var timeline = record.getTimeline().stream()
                .map(entry -> entry.getTimestamp() + " - " + entry.getEventDescription())
                .collect(Collectors.toList());
        return new AccessRecordResource(
                record.getId(),
                record.getCorrelationId().correlationId(),
                record.getDecision(),
                record.isSealed(),
                timeline,
                record.getCreatedAt()
        );
    }
}
