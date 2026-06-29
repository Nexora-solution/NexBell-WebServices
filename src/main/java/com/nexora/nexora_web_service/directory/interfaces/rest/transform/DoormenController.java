package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.DoormanBuildingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/directory/doormen")
@Tag(name = "Doormen", description = "Doorman profile (name, phone, photo) and building affiliation")
public class DoormenController {

    private final DoormanBuildingRepository doormanBuildingRepository;
    private final BuildingDirectoryRepository buildingDirectoryRepository;

    public DoormenController(DoormanBuildingRepository doormanBuildingRepository,
                             BuildingDirectoryRepository buildingDirectoryRepository) {
        this.doormanBuildingRepository = doormanBuildingRepository;
        this.buildingDirectoryRepository = buildingDirectoryRepository;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a doorman's profile + affiliated building name")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long userId) {
        var opt = doormanBuildingRepository.findByUserId(userId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        String buildingName = buildingDirectoryRepository.findById(d.getBuildingId())
                .map(b -> b.getName()).orElse(null);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", d.getUserId());
        body.put("buildingId", d.getBuildingId());
        body.put("buildingName", buildingName);
        body.put("loginEmail", d.getLoginEmail());
        body.put("personalEmail", d.getPersonalEmail());
        body.put("fullName", d.getFullName());
        body.put("phone", d.getPhone());
        body.put("photoUrl", d.getPhotoUrl());
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update a doorman's editable profile (name, phone, photo)")
    public ResponseEntity<Void> updateProfile(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        var opt = doormanBuildingRepository.findByUserId(userId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var d = opt.get();
        if (body.containsKey("fullName")) d.setFullName(body.get("fullName") == null ? null : body.get("fullName").toString());
        if (body.containsKey("phone"))    d.setPhone(body.get("phone") == null ? null : body.get("phone").toString());
        if (body.containsKey("photoUrl")) d.setPhotoUrl(body.get("photoUrl") == null ? null : body.get("photoUrl").toString());
        doormanBuildingRepository.save(d);
        return ResponseEntity.ok().build();
    }
}
