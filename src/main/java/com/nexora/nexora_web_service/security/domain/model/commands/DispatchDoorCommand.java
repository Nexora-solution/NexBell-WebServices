package com.nexora.nexora_web_service.security.domain.model.commands;

import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;

public record DispatchDoorCommand(CommandType commandType) {
    public DispatchDoorCommand {
        if (commandType == null) throw new IllegalArgumentException("Command type cannot be null");
    }
}
