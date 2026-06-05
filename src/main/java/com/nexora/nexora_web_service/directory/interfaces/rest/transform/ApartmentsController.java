package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.commands.AssignResidentCommand;
import com.nexora.nexora_web_service.directory.domain.model.commands.CreateApartmentCommand;
import com.nexora.nexora_web_service.directory.domain.model.entities.Apartment;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ContactChannel;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ResidentDocument;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.AssignResidentResource;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.CreateApartmentResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/directory/apartments")
@Tag(name = "Apartments", description = "Apartment setup and resident assignments")
public class ApartmentsController {

    private final DirectoryCommandService directoryCommandService;

    public ApartmentsController(DirectoryCommandService directoryCommandService) {
        this.directoryCommandService = directoryCommandService;
    }

    @PostMapping
    @Operation(summary = "Register a new apartment unit in the building")
    public ResponseEntity<Apartment> createApartment(@Valid @RequestBody CreateApartmentResource resource) {
        var command = new CreateApartmentCommand(resource.buildingId(), resource.code());
        var apartmentOpt = directoryCommandService.handle(command);
        return apartmentOpt.map(apartment -> ResponseEntity.status(HttpStatus.CREATED).body(apartment))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}/resident")
    @Operation(summary = "Assign a resident to an apartment unit")
    public ResponseEntity<Apartment> assignResident(@PathVariable Long id, @Valid @RequestBody AssignResidentResource resource) {
        var command = new AssignResidentCommand(
                id,
                resource.userId(),
                resource.fullName(),
                new ResidentDocument(resource.documentNumber()),
                new ContactChannel(resource.email(), resource.phone())
        );
        var apartmentOpt = directoryCommandService.handle(command);
        return apartmentOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
