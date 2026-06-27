package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.security.domain.model.commands.TriggerTamperingAlarmCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.model.queries.GetActiveAlarmsQuery;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import com.nexora.nexora_web_service.security.domain.services.SecurityQueryService;
import com.nexora.nexora_web_service.security.infrastructure.gateway.SseAlarmBroadcastGateway;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.SecurityAlarmResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/security/alarms")
@Tag(name = "Security Alarms", description = "Endpoints for tamper/force alarms")
public class SecurityAlarmController {

    private static final Logger log = LoggerFactory.getLogger(SecurityAlarmController.class);

    private final SecurityCommandService securityCommandService;
    private final SecurityQueryService securityQueryService;

    public SecurityAlarmController(SecurityCommandService securityCommandService,
                                   SecurityQueryService securityQueryService) {
        this.securityCommandService = securityCommandService;
        this.securityQueryService = securityQueryService;
    }

    public record TamperingRequest(String sensorType) {}

    @PostMapping("/tampering")
    @Operation(summary = "Signal a tampering/vibration alarm event from the physical door")
    public ResponseEntity<SecurityAlarm> triggerTampering(@org.springframework.web.bind.annotation.RequestBody TamperingRequest request) {
        var command = new TriggerTamperingAlarmCommand(request.sensorType());
        var alarmOpt = securityCommandService.handle(command);
        return alarmOpt.map(alarm -> ResponseEntity.status(HttpStatus.CREATED).body(alarm))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/active")
    @Operation(summary = "List the currently active (TRIGGERED) security alarms")
    public ResponseEntity<List<SecurityAlarmResource>> getActiveAlarms() {
        var alarms = securityQueryService.handle(new GetActiveAlarmsQuery());
        var resources = alarms.stream()
                .map(a -> new SecurityAlarmResource(
                        a.getId(),
                        a.getSensorType() != null ? a.getSensorType().name() : null,
                        a.getStatus(),
                        a.getTriggeredAt()))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time security alarm events (SSE)")
    public SseEmitter subscribeToAlarmStream() {
        SseEmitter emitter = new SseEmitter(24 * 60 * 60 * 1000L); // 24 hours
        SseAlarmBroadcastGateway.addEmitter(emitter);
        try {
            emitter.send(SseEmitter.event().name("init").data("Connected to NexBell Security Alarm Stream"));
        } catch (IOException e) {
            log.error("Failed to send init SSE event", e);
        }
        return emitter;
    }
}
