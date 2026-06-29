package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.commands.CreateBuildingCommand;
import com.nexora.nexora_web_service.directory.domain.model.entities.Apartment;
import com.nexora.nexora_web_service.directory.domain.model.entities.BuildingDirectory;
import com.nexora.nexora_web_service.directory.domain.model.entities.DoormanBuilding;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.DoormanBuildingRepository;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.CreateBuildingResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/directory/buildings")
@Tag(name = "Buildings", description = "Building directory administration")
public class BuildingsController {

    private final DirectoryCommandService directoryCommandService;
    private final BuildingDirectoryRepository buildingDirectoryRepository;
    private final ApartmentRepository apartmentRepository;
    private final DoormanBuildingRepository doormanBuildingRepository;

    public BuildingsController(DirectoryCommandService directoryCommandService,
                               BuildingDirectoryRepository buildingDirectoryRepository,
                               ApartmentRepository apartmentRepository,
                               DoormanBuildingRepository doormanBuildingRepository) {
        this.directoryCommandService = directoryCommandService;
        this.buildingDirectoryRepository = buildingDirectoryRepository;
        this.apartmentRepository = apartmentRepository;
        this.doormanBuildingRepository = doormanBuildingRepository;
    }

    @GetMapping
    @Operation(summary = "List all registered buildings in the directory")
    public ResponseEntity<List<BuildingDirectory>> getAllBuildings() {
        return ResponseEntity.ok(buildingDirectoryRepository.findAll());
    }

    @GetMapping("/{id}/apartments")
    @Operation(summary = "List all apartments for a given building")
    public ResponseEntity<List<Apartment>> getApartmentsByBuilding(@PathVariable Long id) {
        return ResponseEntity.ok(apartmentRepository.findByBuildingId(id));
    }

    @PostMapping
    @Operation(summary = "Register a new building in the directory")
    public ResponseEntity<BuildingDirectory> createBuilding(@Valid @RequestBody CreateBuildingResource resource) {
        var command = new CreateBuildingCommand(resource.name(), resource.address());
        var buildingOpt = directoryCommandService.handle(command);
        return buildingOpt.map(building -> ResponseEntity.status(HttpStatus.CREATED).body(building))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/doormen")
    @Operation(summary = "Assign a doorman (IAM user) to a building")
    public ResponseEntity<Void> assignDoorman(@PathVariable Long id,
                                              @RequestBody Map<String, Object> body) {
        Object rawUserId = body.get("userId");
        if (rawUserId == null) return ResponseEntity.badRequest().build();
        Long userId = Long.valueOf(rawUserId.toString());
        if (!buildingDirectoryRepository.existsById(id)) return ResponseEntity.notFound().build();

        String personalEmail = body.get("personalEmail") == null ? null : body.get("personalEmail").toString();
        String loginEmail = body.get("loginEmail") == null ? null : body.get("loginEmail").toString();

        var doorman = doormanBuildingRepository.findByUserId(userId)
                .orElseGet(() -> new DoormanBuilding(userId, id));
        doorman.setBuildingId(id);
        if (personalEmail != null) doorman.setPersonalEmail(personalEmail);
        if (loginEmail != null) doorman.setLoginEmail(loginEmail);
        doormanBuildingRepository.save(doorman);
        return ResponseEntity.ok().build();
    }
}
