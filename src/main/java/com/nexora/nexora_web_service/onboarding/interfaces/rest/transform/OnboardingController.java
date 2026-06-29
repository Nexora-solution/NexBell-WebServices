package com.nexora.nexora_web_service.onboarding.interfaces.rest.transform;

import com.nexora.nexora_web_service.onboarding.domain.model.commands.ClaimDoormanCredentialsCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ProvisionContractCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.ContractProvisionResult;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.GeneratedCredential;
import com.nexora.nexora_web_service.onboarding.domain.services.OnboardingCommandService;
import com.nexora.nexora_web_service.onboarding.interfaces.rest.resources.ClaimCredentialsResource;
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

    public OnboardingController(OnboardingCommandService onboardingCommandService) {
        this.onboardingCommandService = onboardingCommandService;
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
