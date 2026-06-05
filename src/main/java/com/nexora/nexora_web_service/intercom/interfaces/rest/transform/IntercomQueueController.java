package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.intercom.domain.model.entities.IntercomQueueItem;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetPendingQueueQuery;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetVisitDetailQuery;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomQueryService;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.VisitRequestDetailResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/intercom")
@Tag(name = "Intercom Queue", description = "Operational queue administration for doormen")
public class IntercomQueueController {

    private final IntercomQueryService queryService;

    public IntercomQueueController(IntercomQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/queue/pending")
    @Operation(summary = "Get the list of visit requests currently pending check-in")
    public ResponseEntity<List<IntercomQueueItem>> getPendingQueue() {
        var query = new GetPendingQueueQuery();
        var pendingItems = queryService.handle(query);
        return ResponseEntity.ok(pendingItems);
    }

    @GetMapping("/visit-requests/{id}")
    @Operation(summary = "Get the details of a specific visit request by its ID")
    public ResponseEntity<VisitRequestDetailResource> getVisitDetail(@PathVariable Long id) {
        var query = new GetVisitDetailQuery(id);
        var requestOpt = queryService.handle(query);
        return requestOpt.map(req -> ResponseEntity.ok(toDetailResource(req)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
