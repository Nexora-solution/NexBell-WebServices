package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.queries.GetResidentByApartmentQuery;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryQueryService;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.ResidentProfileResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ApartmentCode;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directory/apartments")
@Tag(name = "Directory Queries", description = "Directory read side queries")
public class DirectoryQueryController {

    private final DirectoryQueryService directoryQueryService;
    private final ApartmentRepository apartmentRepository;
    private final ResidentDirectoryRepository residentDirectoryRepository;

    public DirectoryQueryController(DirectoryQueryService directoryQueryService,
                                    ApartmentRepository apartmentRepository,
                                    ResidentDirectoryRepository residentDirectoryRepository) {
        this.directoryQueryService = directoryQueryService;
        this.apartmentRepository = apartmentRepository;
        this.residentDirectoryRepository = residentDirectoryRepository;
    }

    @GetMapping("/{id}/resident")
    @Operation(summary = "Resolve the resident profile associated to an apartment")
    public ResponseEntity<ResidentProfileResource> getResidentByApartment(@PathVariable Long id) {
        var query = new GetResidentByApartmentQuery(id);
        var profileOpt = directoryQueryService.handle(query);
        return profileOpt.map(profile -> ResponseEntity.ok(new ResidentProfileResource(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getDocument().documentNumber(),
                profile.getContact().email(),
                profile.getContact().phone(),
                null,
                id
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/resident")
    @Operation(summary = "Resolve the resident profile associated to an apartment code, optionally filtered by building")
    public ResponseEntity<ResidentProfileResource> getResidentByApartmentCode(
            @RequestParam String code,
            @RequestParam(required = false) Long buildingId) {
        var code_ = new ApartmentCode(code);
        var apartmentOpt = buildingId != null
                ? apartmentRepository.findByBuildingIdAndCode(buildingId, code_)
                : apartmentRepository.findByCode(code_);
        if (apartmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var apartment = apartmentOpt.get();
        if (apartment.getResidentId() == null) {
            return ResponseEntity.notFound().build();
        }
        var profileOpt = residentDirectoryRepository.findById(apartment.getResidentId());
        return profileOpt.map(profile -> ResponseEntity.ok(new ResidentProfileResource(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getDocument().documentNumber(),
                profile.getContact().email(),
                profile.getContact().phone(),
                apartment.getCode().code(),
                apartment.getId()
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
