package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.security.domain.model.commands.TriggerTamperingAlarmCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/alarms")
@Tag(name = "Security Alarms", description = "Endpoints for tamper/force alarms")
public class SecurityAlarmController {

    private final SecurityCommandService securityCommandService;

    public SecurityAlarmController(SecurityCommandService securityCommandService) {
        this.securityCommandService = securityCommandService;
    }

    @PostMapping("/tampering")
    @Operation(summary = "Signal a tampering/vibration alarm event from the physical door")
    public ResponseEntity<SecurityAlarm> triggerTampering() {
        var command = new TriggerTamperingAlarmCommand();
        var alarmOpt = securityCommandService.handle(command);
        return alarmOpt.map(alarm -> ResponseEntity.status(HttpStatus.CREATED).body(alarm))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
