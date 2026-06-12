package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.commands.CreateBuildingCommand;
import com.nexora.nexora_web_service.directory.domain.model.entities.BuildingDirectory;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.CreateBuildingResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/directory/buildings")
@Tag(name = "Buildings", description = "Building directory administration")
public class BuildingsController {

    private final DirectoryCommandService directoryCommandService;
    private final BuildingDirectoryRepository buildingDirectoryRepository;

    public BuildingsController(DirectoryCommandService directoryCommandService,
                               BuildingDirectoryRepository buildingDirectoryRepository) {
        this.directoryCommandService = directoryCommandService;
        this.buildingDirectoryRepository = buildingDirectoryRepository;
    }

    @GetMapping
    @Operation(summary = "List all registered buildings in the directory")
    public ResponseEntity<List<BuildingDirectory>> getAllBuildings() {
        return ResponseEntity.ok(buildingDirectoryRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Register a new building in the directory")
    public ResponseEntity<BuildingDirectory> createBuilding(@Valid @RequestBody CreateBuildingResource resource) {
        var command = new CreateBuildingCommand(resource.name(), resource.address());
        var buildingOpt = directoryCommandService.handle(command);
        return buildingOpt.map(building -> ResponseEntity.status(HttpStatus.CREATED).body(building))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
