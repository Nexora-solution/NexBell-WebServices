package com.nexora.nexora_web_service.directory.domain.model.commands;

public record CreateBuildingCommand(String name, String address) {
    public CreateBuildingCommand {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Building name cannot be empty");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Building address cannot be empty");
    }
}
