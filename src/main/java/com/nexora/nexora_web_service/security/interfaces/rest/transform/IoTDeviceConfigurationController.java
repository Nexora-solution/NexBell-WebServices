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

import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.IoTDeviceRepository;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.IoTDeviceMediaStatusResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/iot")
@Tag(name = "IoT Devices", description = "IoT Hardware configuration settings")
public class IoTDeviceConfigurationController {

    private final SecurityCommandService securityCommandService;
    private final IoTDeviceRepository ioTDeviceRepository;

    public IoTDeviceConfigurationController(SecurityCommandService securityCommandService,
                                             IoTDeviceRepository ioTDeviceRepository) {
        this.securityCommandService = securityCommandService;
        this.ioTDeviceRepository = ioTDeviceRepository;
    }

    @GetMapping("/media/status")
    @Operation(summary = "Get the camera and microphone stream status of a device")
    public ResponseEntity<IoTDeviceMediaStatusResource> getMediaStatus(@RequestParam(required = false) String deviceCode) {
        String code = (deviceCode != null) ? deviceCode : "main-door";
        var device = ioTDeviceRepository.findByDeviceCode(code)
                .orElseGet(() -> ioTDeviceRepository.save(new IoTDevice(code)));
        return ResponseEntity.ok(new IoTDeviceMediaStatusResource(
                device.getDeviceCode(),
                device.isCameraEnabled(),
                device.isMicrophoneEnabled()
        ));
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
