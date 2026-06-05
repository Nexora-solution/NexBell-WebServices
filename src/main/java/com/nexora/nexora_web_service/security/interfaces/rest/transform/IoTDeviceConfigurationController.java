package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.security.domain.model.commands.ToggleDeviceMediaStreamCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.IoTDeviceMediaResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/iot")
@Tag(name = "IoT Devices", description = "IoT Hardware configuration settings")
public class IoTDeviceConfigurationController {

    private final SecurityCommandService securityCommandService;

    public IoTDeviceConfigurationController(SecurityCommandService securityCommandService) {
        this.securityCommandService = securityCommandService;
    }

    @PostMapping("/media/toggle")
    @Operation(summary = "Suspend or resume camera/microphone streams for user privacy")
    public ResponseEntity<IoTDevice> toggleMedia(@Valid @RequestBody IoTDeviceMediaResource resource) {
        var command = new ToggleDeviceMediaStreamCommand(resource.deviceCode(), resource.camera(), resource.microphone());
        var deviceOpt = securityCommandService.handle(command);
        return deviceOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
