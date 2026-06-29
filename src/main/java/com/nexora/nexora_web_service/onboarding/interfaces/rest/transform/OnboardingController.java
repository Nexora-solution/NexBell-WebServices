package com.nexora.nexora_web_service.onboarding.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ClaimDoormanCredentialsCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ClaimResidentCredentialsCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ProvisionContractCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.ContractProvisionResult;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.GeneratedCredential;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.ResidentClaimOutcome;
import com.nexora.nexora_web_service.onboarding.domain.services.OnboardingCommandService;
import com.nexora.nexora_web_service.onboarding.interfaces.rest.resources.ClaimCredentialsResource;
import com.nexora.nexora_web_service.onboarding.interfaces.rest.resources.ClaimResidentCredentialsResource;
import com.nexora.nexora_web_service.onboarding.interfaces.rest.resources.ContractProvisionResultResource;
import com.nexora.nexora_web_service.onboarding.interfaces.rest.resources.CredentialResource;
import com.nexora.nexora_web_service.onboarding.interfaces.rest.resources.ProvisionContractResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding", description = "Contract provisioning: building + building-scoped credentials")
public class OnboardingController {

    private final OnboardingCommandService onboardingCommandService;
    private final BuildingDirectoryRepository buildings;
    private final ApartmentRepository apartments;
    private final ResidentDirectoryRepository residents;

    public OnboardingController(OnboardingCommandService onboardingCommandService,
                                BuildingDirectoryRepository buildings,
                                ApartmentRepository apartments,
                                ResidentDirectoryRepository residents) {
        this.onboardingCommandService = onboardingCommandService;
        this.buildings = buildings;
        this.apartments = apartments;
        this.residents = residents;
    }

    @PostMapping("/contracts")
    @Operation(summary = "Provision a whole building from one contract (atomic, building-scoped credentials)")
    public ResponseEntity<ContractProvisionResultResource> provision(@Valid @RequestBody ProvisionContractResource resource) {
        var command = new ProvisionContractCommand(
                resource.buildingName(),
                resource.address(),
                resource.district(),
                resource.imageUrl(),
                resource.floors() == null ? 1 : resource.floors(),
                resource.apartments() == null ? 0 : resource.apartments(),
                resource.doormen() == null ? 0 : resource.doormen(),
                resource.doormanPersonalEmails(),
                resource.phone()
        );
        var result = onboardingCommandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResource(result));
    }

    @PostMapping("/credentials/claim")
    @Operation(summary = "A doorman reclaims their credentials by building + personal email")
    public ResponseEntity<Map<String, Object>> claim(@Valid @RequestBody ClaimCredentialsResource resource) {
        var command = new ClaimDoormanCredentialsCommand(resource.buildingId(), resource.personalEmail());
        boolean sent = onboardingCommandService.handle(command);
        if (!sent) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "sent", false,
                    "message", "No encontramos un portero con ese correo en este edificio."
            ));
        }
        return ResponseEntity.ok(Map.of(
                "sent", true,
                "message", "Te enviamos tus credenciales a tu correo personal."
        ));
    }

    @PostMapping("/credentials/claim-resident")
    @Operation(summary = "A resident reclaims their credentials by building + apartment + personal email")
    public ResponseEntity<Map<String, Object>> claimResident(@Valid @RequestBody ClaimResidentCredentialsResource resource) {
        var command = new ClaimResidentCredentialsCommand(resource.buildingName(), resource.apartmentCode(), resource.personalEmail());
        ResidentClaimOutcome outcome = onboardingCommandService.handle(command);
        return switch (outcome) {
            case SENT -> ResponseEntity.ok(Map.of(
                    "sent", true,
                    "message", "Te enviamos tus credenciales a tu correo personal."
            ));
            case ALREADY_ACTIVE -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "sent", false,
                    "message", "Este departamento ya fue activado. Si eres el residente, inicia sesión; si olvidaste tu contraseña usa 'Recuperar contraseña'."
            ));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "sent", false,
                    "message", "No encontramos ese departamento en ese edificio. Revisa el nombre del edificio y el número de departamento."
            ));
        };
    }

    @GetMapping("/buildings")
    @Operation(summary = "Public list of buildings for the mobile credential-request autocomplete")
    public ResponseEntity<List<Map<String, Object>>> listBuildings() {
        var result = buildings.findAll().stream()
                .map(b -> Map.<String, Object>of(
                        "id", b.getId(),
                        "name", b.getName() == null ? "" : b.getName(),
                        "district", b.getDistrict() == null ? "" : b.getDistrict()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/buildings/{buildingId}/apartments")
    @Operation(summary = "Public list of a building's apartments, flagging which are still claimable (pending)")
    public ResponseEntity<List<Map<String, Object>>> listApartments(@PathVariable Long buildingId) {
        var result = apartments.findByBuildingId(buildingId).stream()
                .map(apt -> Map.<String, Object>of(
                        "code", apt.getCode() == null ? "" : apt.getCode().code(),
                        "claimable", isClaimable(apt.getResidentId())
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    /** Claimable = the apartment has a resident profile that has not activated yet (no phone on file). */
    private boolean isClaimable(Long residentProfileId) {
        if (residentProfileId == null) return false;
        return residents.findById(residentProfileId)
                .map(ResidentDirectoryProfile::getContact)
                .map(contact -> contact.phone() == null || contact.phone().isBlank())
                .orElse(false);
    }

    private ContractProvisionResultResource toResource(ContractProvisionResult result) {
        return new ContractProvisionResultResource(
                result.buildingId(),
                result.buildingLetter(),
                toCredentialResources(result.residents()),
                toCredentialResources(result.doormen())
        );
    }

    private List<CredentialResource> toCredentialResources(List<GeneratedCredential> creds) {
        return creds.stream()
                .map(c -> new CredentialResource(c.label(), c.email(), c.password(), c.personalEmail()))
                .toList();
    }
}
