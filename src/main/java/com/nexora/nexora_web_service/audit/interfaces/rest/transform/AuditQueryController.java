package com.nexora.nexora_web_service.audit.interfaces.rest.transform;

import com.nexora.nexora_web_service.audit.domain.model.entities.AccessRecord;
import com.nexora.nexora_web_service.audit.domain.model.entities.AccessTimelineEntry;
import com.nexora.nexora_web_service.audit.domain.model.queries.GetAccessRecordsByResidentQuery;
import com.nexora.nexora_web_service.audit.domain.model.queries.GetHardwareLogsQuery;
import com.nexora.nexora_web_service.audit.domain.model.queries.SearchAccessRecordsQuery;
import com.nexora.nexora_web_service.audit.domain.services.AuditQueryService;
import com.nexora.nexora_web_service.audit.interfaces.rest.resources.AccessRecordResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Queries", description = "Querying logs and hardware activity timeline")
public class AuditQueryController {

    private final AuditQueryService queryService;

    public AuditQueryController(AuditQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/access-records")
    @Operation(summary = "Search/list all access records")
    public ResponseEntity<List<AccessRecordResource>> searchAccessRecords() {
        var query = new SearchAccessRecordsQuery();
        var records = queryService.handle(query);
        var resources = records.stream().map(this::toResource).collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/access-records/resident/{id}")
    @Operation(summary = "Get access record history for a resident ID")
    public ResponseEntity<List<AccessRecordResource>> getAccessRecordsByResident(@PathVariable Long id) {
        var query = new GetAccessRecordsByResidentQuery(id);
        var records = queryService.handle(query);
        var resources = records.stream().map(this::toResource).collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/hardware-logs")
    @Operation(summary = "Get the hardware events activity logs (door commands and statuses)")
    public ResponseEntity<List<AccessTimelineEntry>> getHardwareLogs() {
        var query = new GetHardwareLogsQuery();
        var logs = queryService.handle(query);
        return ResponseEntity.ok(logs);
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
