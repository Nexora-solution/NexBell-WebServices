package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.intercom.domain.model.commands.CancelPreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.CreatePreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.commands.UpdatePreRegisteredVisitCommand;
import com.nexora.nexora_web_service.intercom.domain.model.entities.PreRegisteredVisit;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.PreRegisteredVisitRepository;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.CreatePreRegisteredVisitResource;
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

    public ResidentPreRegistrationController(IntercomCommandService commandService,
                                             PreRegisteredVisitRepository preRegisteredVisitRepository,
                                             ResidentDirectoryRepository residentDirectoryRepository,
                                             ApartmentRepository apartmentRepository) {
        this.commandService = commandService;
        this.preRegisteredVisitRepository = preRegisteredVisitRepository;
        this.residentDirectoryRepository = residentDirectoryRepository;
        this.apartmentRepository = apartmentRepository;
    }

    @GetMapping
    @Operation(summary = "List pre-registered visits, optionally filtered by building")
    public ResponseEntity<List<Map<String, Object>>> getAllPreRegisteredVisits(
            @RequestParam(required = false) Long buildingId) {
        var visits = preRegisteredVisitRepository.findAllByOrderByCreatedAtDesc();
        var result = visits.stream()
            .map(v -> {
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
                return Map.<String, Object>of(
                    "id", v.getId(),
                    "visitorName", v.getVisitorName(),
                    "visitorDocument", v.getVisitorDocument() != null ? v.getVisitorDocument() : "",
                    "expectedAt", v.getExpectedAt(),
                    "isActive", v.isActive(),
                    "residentId", v.getResidentId(),
                    "residentName", residentName != null ? residentName : "",
                    "apartmentCode", aptCode != null ? aptCode : "",
                    "buildingId", aptBuildingId != null ? aptBuildingId : 0L
                );
            })
            .filter(v -> buildingId == null || buildingId.equals(v.get("buildingId")))
            .toList();
        return ResponseEntity.ok(result);
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
    @Operation(summary = "Delete a pre-registered visit")
    public ResponseEntity<Void> deletePreRegisteredVisit(@PathVariable Long id) {
        if (!preRegisteredVisitRepository.existsById(id)) return ResponseEntity.notFound().build();
        preRegisteredVisitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
