package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.commands.UpdateResidentContactCommand;
import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ContactChannel;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.ResidentProfileResource;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.UpdateResidentContactResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/directory/residents")
@Tag(name = "Residents", description = "Resident profile management")
public class ResidentsController {

    private final DirectoryCommandService directoryCommandService;
    private final ResidentDirectoryRepository residentDirectoryRepository;
    private final ApartmentRepository apartmentRepository;

    public ResidentsController(DirectoryCommandService directoryCommandService,
                               ResidentDirectoryRepository residentDirectoryRepository,
                               ApartmentRepository apartmentRepository) {
        this.directoryCommandService = directoryCommandService;
        this.residentDirectoryRepository = residentDirectoryRepository;
        this.apartmentRepository = apartmentRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a resident profile by their directory profile ID")
    public ResponseEntity<ResidentProfileResource> getResident(@PathVariable Long id) {
        var profileOpt = residentDirectoryRepository.findById(id);
        if (profileOpt.isEmpty()) return ResponseEntity.notFound().build();
        var profile = profileOpt.get();
        var apts = apartmentRepository.findByResidentId(id);
        var lastApt = apts.isEmpty() ? null : apts.get(apts.size() - 1);
        String apartmentCode = lastApt == null ? null : lastApt.getCode().code();
        Long apartmentId = lastApt == null ? null : lastApt.getId();
        return ResponseEntity.ok(new ResidentProfileResource(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getDocument().documentNumber(),
                profile.getContact().email(),
                profile.getContact().phone(),
                apartmentCode,
                apartmentId,
                profile.getPhotoUrl()
        ));
    }

    @PutMapping("/{id}/contact")
    @Operation(summary = "Update resident's profile: name, contact, and apartment code")
    public ResponseEntity<ResidentDirectoryProfile> updateContact(@PathVariable Long id, @RequestBody UpdateResidentContactResource resource) {
        var profileOpt = residentDirectoryRepository.findById(id);
        if (profileOpt.isEmpty()) return ResponseEntity.notFound().build();
        var profile = profileOpt.get();

        // Update full name if provided
        if (resource.fullName() != null && !resource.fullName().isBlank()) {
            profile.setFullName(resource.fullName());
        }

        // Update contact if provided
        String email = resource.email() != null ? resource.email() : profile.getContact().email();
        String phone = resource.phone() != null ? resource.phone() : profile.getContact().phone();
        profile.setContact(new ContactChannel(email, phone));

        // Update profile photo if provided
        if (resource.photoUrl() != null) {
            profile.setPhotoUrl(resource.photoUrl());
        }

        residentDirectoryRepository.save(profile);

        // Update apartment code if provided
        if (resource.apartmentCode() != null && !resource.apartmentCode().isBlank()) {
            var apts = apartmentRepository.findByResidentId(id);
            if (!apts.isEmpty()) {
                var apt = apts.get(apts.size() - 1);
                apt.setCode(new com.nexora.nexora_web_service.directory.domain.model.valueobjects.ApartmentCode(resource.apartmentCode()));
                apartmentRepository.save(apt);
            }
        }

        return ResponseEntity.ok(profile);
    }
}
