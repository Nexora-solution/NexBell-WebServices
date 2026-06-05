package com.nexora.nexora_web_service.security.infrastructure.gateway;

import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import com.nexora.nexora_web_service.security.domain.services.IoTCommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IoTHttpCommandGateway implements IoTCommandGateway {

    private static final Logger log = LoggerFactory.getLogger(IoTHttpCommandGateway.class);

    @Override
    public boolean send(DoorCommand command) {
        log.info("Sending command '{}' to IoT door controller...", command.getCommandType());
        return true;
    }
}
