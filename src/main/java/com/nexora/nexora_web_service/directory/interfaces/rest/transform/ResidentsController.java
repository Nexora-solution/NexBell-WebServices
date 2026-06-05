package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.commands.UpdateResidentContactCommand;
import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ContactChannel;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
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

    public ResidentsController(DirectoryCommandService directoryCommandService) {
        this.directoryCommandService = directoryCommandService;
    }

    @PutMapping("/{id}/contact")
    @Operation(summary = "Update resident's contact details")
    public ResponseEntity<ResidentDirectoryProfile> updateContact(@PathVariable Long id, @Valid @RequestBody UpdateResidentContactResource resource) {
        var command = new UpdateResidentContactCommand(id, new ContactChannel(resource.email(), resource.phone()));
        var profileOpt = directoryCommandService.handle(command);
        return profileOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
