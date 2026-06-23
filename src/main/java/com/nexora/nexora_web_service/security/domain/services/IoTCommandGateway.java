package com.nexora.nexora_web_service.security.domain.services;

import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;

public interface IoTCommandGateway {
    boolean send(DoorCommand command);

    /** Forwards a camera stream start/stop command to the edge service. */
    boolean sendMediaToggle(boolean camera, boolean microphone);
}
