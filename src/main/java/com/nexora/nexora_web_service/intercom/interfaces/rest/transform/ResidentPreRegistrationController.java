package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.intercom.application.internal.outboundservices.acl.ExternalDirectoryService;
import com.nexora.nexora_web_service.intercom.domain.model.commands.CancelPreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.CreatePreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.RegisterPreRegisteredDecisionCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.UpdatePreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.entities.PreRegisteredVisit;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.domain.services.NotificationGateway;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.PreRegisteredVisitRepository;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.CreatePreRegisteredVisitResource;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.RegisterPreRegisteredDecisionResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intercom/pre-registered-visits")
@Tag(name = "Pre-registered Visits", description = "Management of pre-authorized expected visitors")
public class ResidentPreRegistrationController {

    private final IntercomCommandService commandService;
    private final PreRegisteredVisitRepository preRegisteredVisitRepository;
    private final ResidentDirectoryRepository residentDirectoryRepository;
    private final ApartmentRepository apartmentRepository;
    private final ExternalDirectoryService directoryService;
    private final NotificationGateway notificationGateway;

    public ResidentPreRegistrationController(IntercomCommandService commandService,
                                             PreRegisteredVisitRepository preRegisteredVisitRepository,
                                             ResidentDirectoryRepository residentDirectoryRepository,
                                             ApartmentRepository apartmentRepository,
                                             ExternalDirectoryService directoryService,
                                             NotificationGateway notificationGateway) {
        this.commandService = commandService;
        this.preRegisteredVisitRepository = preRegisteredVisitRepository;
        this.residentDirectoryRepository = residentDirectoryRepository;
        this.apartmentRepository = apartmentRepository;
        this.directoryService = directoryService;
        this.notificationGateway = notificationGateway;
    }

    @GetMapping
    @Operation(summary = "List pre-registered visits, optionally filtered by building or resident")
    public ResponseEntity<List<Map<String, Object>>> getAllPreRegisteredVisits(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long residentId) {
        var visits = preRegisteredVisitRepository.findAllByOrderByCreatedAtDesc();
        var result = visits.stream()
            .map(this::toEnrichedMap)
            .filter(v -> buildingId == null || buildingId.equals(v.get("buildingId")))
            .filter(v -> residentId == null || residentId.equals(v.get("residentId")))
            .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single pre-registered visit by ID")
    public ResponseEntity<Map<String, Object>> getPreRegisteredVisit(@PathVariable Long id) {
        var visitOpt = preRegisteredVisitRepository.findById(id);
        return visitOpt.map(v -> ResponseEntity.ok(toEnrichedMap(v)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String, Object> toEnrichedMap(PreRegisteredVisit v) {
        String aptCode = null;
        String residentName = null;
        Long aptBuildingId = null;
        var profileOpt = residentDirectoryRepository.findById(v.getResidentId());
        if (profileOpt.isPresent()) {
            residentName = profileOpt.get().getFullName();
            var apts = apartmentRepository.findByResidentId(v.getResidentId());
            if (!apts.isEmpty()) {
                var apt = apts.get(apts.size() - 1);
                aptCode = apt.getCode().code();
                aptBuildingId = apt.getBuildingId();
            }
        }
        return Map.<String, Object>ofEntries(
            Map.entry("id", v.getId()),
            Map.entry("visitorName", v.getVisitorName()),
            Map.entry("visitorDocument", v.getVisitorDocument() != null ? v.getVisitorDocument() : ""),
            Map.entry("visitorPhotoUrl", v.getVisitorPhotoUrl() != null ? v.getVisitorPhotoUrl() : ""),
            Map.entry("expectedAt", v.getExpectedAt()),
            Map.entry("isActive", v.isActive()),
            Map.entry("status", v.getStatus()),
            Map.entry("registeredBy", v.getRegisteredBy()),
            Map.entry("residentId", v.getResidentId()),
            Map.entry("residentName", residentName != null ? residentName : ""),
            Map.entry("apartmentCode", aptCode != null ? aptCode : ""),
            Map.entry("buildingId", aptBuildingId != null ? aptBuildingId : 0L)
        );
    }

    @PostMapping
    @Operation(summary = "Register a new expected pre-registered visit")
    public ResponseEntity<PreRegisteredVisit> createPreRegisteredVisit(@Valid @RequestBody CreatePreRegisteredVisitResource resource) {
        var command = new CreatePreRegisteredVisitCommand(
                resource.residentId(),
                resource.visitorName(),
                resource.visitorDocument(),
                resource.visitorPhotoUrl(),
                resource.expectedAt(),
                resource.registeredBy()
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
                resource.visitorPhotoUrl(),
                resource.expectedAt()
        );
        var visitOpt = commandService.handle(command);
        return visitOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pre-registered visit")
    public ResponseEntity<Void> deletePreRegisteredVisit(@PathVariable Long id) {
        if (!preRegisteredVisitRepository.existsById(id)) return ResponseEntity.notFound().build();
        preRegisteredVisitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notify")
    @Operation(summary = "Send a real push notification to the resident about this pre-registered visit")
    public ResponseEntity<?> notifyResidentForPreRegisteredVisit(@PathVariable Long id) {
        var visitOpt = preRegisteredVisitRepository.findById(id);
        if (visitOpt.isEmpty()) return ResponseEntity.notFound().build();
        var visit = visitOpt.get();

        var fcmTokenOpt = directoryService.fetchResidentFcmTokenById(visit.getResidentId());
        if (fcmTokenOpt.isEmpty()) {
            return ResponseEntity.status(422).body("Resident has no registered device for push notifications");
        }

        var notification = new com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch("SCHEDULED");
        boolean sent = notificationGateway.send(notification, fcmTokenOpt.get(), visit.getId(), visit.getVisitorName(), "PRE_REGISTERED_VISIT");
        if (!sent) {
            return ResponseEntity.status(502).body("Failed to send push notification");
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/decision")
    @Operation(summary = "Register the resident's decision (APPROVED/REJECTED) for a pre-registered visit")
    public ResponseEntity<PreRegisteredVisit> registerDecision(@PathVariable Long id, @Valid @RequestBody RegisterPreRegisteredDecisionResource resource) {
        var command = new RegisterPreRegisteredDecisionCommand(id, resource.decision());
        var visitOpt = commandService.handle(command);
        return visitOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
