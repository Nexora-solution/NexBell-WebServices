package com.nexora.nexora_web_service.security.domain.services;

import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;

public interface IoTCommandGateway {
    boolean send(DoorCommand command);
}
