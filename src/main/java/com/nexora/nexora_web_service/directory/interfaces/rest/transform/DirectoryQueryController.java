package com.nexora.nexora_web_service.directory.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.queries.GetResidentByApartmentQuery;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryQueryService;
import com.nexora.nexora_web_service.directory.interfaces.rest.resources.ResidentProfileResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directory/apartments")
@Tag(name = "Directory Queries", description = "Directory read side queries")
public class DirectoryQueryController {

    private final DirectoryQueryService directoryQueryService;

    public DirectoryQueryController(DirectoryQueryService directoryQueryService) {
        this.directoryQueryService = directoryQueryService;
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
                profile.getContact().phone()
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
