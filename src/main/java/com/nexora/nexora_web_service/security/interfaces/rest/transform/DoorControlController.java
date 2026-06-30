package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.security.domain.model.commands.DispatchDoorCommand;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.DoorCommandResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.DoorCommandRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.IoTDeviceRepository;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.DoorPhysicalStateResource;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.DoorStatusResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/door")
@Tag(name = "Door Control", description = "Endpoints to lock/unlock physical doors")
@PreAuthorize("hasRole('DOORMAN')")
public class DoorControlController {

    private final SecurityCommandService securityCommandService;
    private final DoorCommandRepository doorCommandRepository;
    private final IoTDeviceRepository deviceRepository;

    public DoorControlController(SecurityCommandService securityCommandService,
                                 DoorCommandRepository doorCommandRepository,
                                 IoTDeviceRepository deviceRepository) {
        this.securityCommandService = securityCommandService;
        this.doorCommandRepository = doorCommandRepository;
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/physical-state")
    @Operation(summary = "Get the current physical door state (OPEN/CLOSED) from the MC38 magnetic sensor")
    public ResponseEntity<DoorPhysicalStateResource> getPhysicalState() {
        var deviceOpt = deviceRepository.findByDeviceCode("DEV-ESP32-DOOR01")
                .or(() -> deviceRepository.findAll().stream().findFirst());
        var resource = deviceOpt
                .map(d -> new DoorPhysicalStateResource(
                        d.getDoorState(),
                        d.getDoorStateChangedAt(),
                        "ONLINE".equals(d.getStatus())))
                .orElse(new DoorPhysicalStateResource("CLOSED", null, false));
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/status")
    @Operation(summary = "Get the current lock status of the physical door")
    public ResponseEntity<DoorStatusResource> getStatus() {
        var latestOpt = doorCommandRepository.findFirstByOrderByIdDesc();
        String status = "LOCKED";
        if (latestOpt.isPresent()) {
            if (latestOpt.get().getCommandType() == CommandType.UNLOCK) {
                status = "UNLOCKED";
            }
        }
        return ResponseEntity.ok(new DoorStatusResource(status, true));
    }

    @PostMapping("/unlock")
    @Operation(summary = "Send an unlock signal to the physical door ESP32")
    public ResponseEntity<DoorCommandResource> unlock() {
        var command = new DispatchDoorCommand(CommandType.UNLOCK);
        var doorCmdOpt = securityCommandService.handle(command);
        return doorCmdOpt.map(cmd -> ResponseEntity.ok(new DoorCommandResource(
                cmd.getId(), cmd.getCommandType(), cmd.getStatus(), cmd.getDispatchedAt(), cmd.getConfirmedAt()
        ))).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/lock")
    @Operation(summary = "Send a lock signal to the physical door ESP32")
    public ResponseEntity<DoorCommandResource> lock() {
        var command = new DispatchDoorCommand(CommandType.LOCK);
        var doorCmdOpt = securityCommandService.handle(command);
        return doorCmdOpt.map(cmd -> ResponseEntity.ok(new DoorCommandResource(
                cmd.getId(), cmd.getCommandType(), cmd.getStatus(), cmd.getDispatchedAt(), cmd.getConfirmedAt()
        ))).orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
