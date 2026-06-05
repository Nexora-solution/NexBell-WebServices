package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.security.domain.model.commands.DispatchDoorCommand;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import com.nexora.nexora_web_service.security.interfaces.rest.resources.DoorCommandResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/door")
@Tag(name = "Door Control", description = "Endpoints to lock/unlock physical doors")
public class DoorControlController {

    private final SecurityCommandService securityCommandService;

    public DoorControlController(SecurityCommandService securityCommandService) {
        this.securityCommandService = securityCommandService;
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
