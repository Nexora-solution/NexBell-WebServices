package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.security.domain.model.commands.ProcessMotionDetectionCommand;
import com.nexora.nexora_web_service.security.domain.model.commands.UpdateDoorStateCommand;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("securityIoTIngressController")
@RequestMapping("/api/security/iot")
@Tag(name = "IoT Ingress", description = "IoT hardware sensors ingress")
public class IoTIngressController {

    private final SecurityCommandService securityCommandService;

    public IoTIngressController(SecurityCommandService securityCommandService) {
        this.securityCommandService = securityCommandService;
    }

    @PostMapping("/presence")
    @Operation(summary = "Report ultrasonic presence detection (people near the door)")
    public ResponseEntity<String> presenceDetected() {
        securityCommandService.handle(new ProcessMotionDetectionCommand());
        return ResponseEntity.ok("Presence processed successfully");
    }

    public record DoorStateRequest(String state) {}

    @PostMapping("/door-state")
    @Operation(summary = "Report the physical door state (OPEN/CLOSED) from the MC38 magnetic sensor")
    public ResponseEntity<String> reportDoorState(@RequestBody DoorStateRequest request) {
        try {
            securityCommandService.handle(new UpdateDoorStateCommand(request.state()));
            return ResponseEntity.ok("Door state updated to " + request.state());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
