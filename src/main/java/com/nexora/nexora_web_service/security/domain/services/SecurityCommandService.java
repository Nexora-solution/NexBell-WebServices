package com.nexora.nexora_web_service.security.domain.services;

import com.nexora.nexora_web_service.security.domain.model.commands.*;
import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;

import java.util.Optional;

public interface SecurityCommandService {
    boolean handle(EvaluateAccessCommand command);
    Optional<DoorCommand> handle(DispatchDoorCommand command);
    Optional<IoTDevice> handle(ToggleDeviceMediaStreamCommand command);
    Optional<SecurityAlarm> handle(TriggerTamperingAlarmCommand command);
    void handle(ProcessMotionDetectionCommand command);
}
