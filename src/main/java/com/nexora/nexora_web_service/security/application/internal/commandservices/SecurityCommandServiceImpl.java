package com.nexora.nexora_web_service.security.application.internal.commandservices;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.security.domain.model.commands.*;
import com.nexora.nexora_web_service.security.domain.model.entities.AccessPolicy;
import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.PermissionCode;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.SensorType;
import com.nexora.nexora_web_service.security.domain.services.IoTCommandGateway;
import com.nexora.nexora_web_service.security.domain.services.SecurityCommandService;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.AccessPolicyRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.DoorCommandRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.IoTDeviceRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.SecurityAlarmRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class SecurityCommandServiceImpl implements SecurityCommandService {

    private final AccessPolicyRepository policyRepository;
    private final DoorCommandRepository doorCommandRepository;
    private final IoTDeviceRepository deviceRepository;
    private final SecurityAlarmRepository alarmRepository;
    private final IoTCommandGateway commandGateway;

    public SecurityCommandServiceImpl(AccessPolicyRepository policyRepository,
                                       DoorCommandRepository doorCommandRepository,
                                       IoTDeviceRepository deviceRepository,
                                       SecurityAlarmRepository alarmRepository,
                                       IoTCommandGateway commandGateway) {
        this.policyRepository = policyRepository;
        this.doorCommandRepository = doorCommandRepository;
        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
        this.commandGateway = commandGateway;
    }

    @PostConstruct
    public void initDefaultPolicies() {
        if (policyRepository.count() == 0) {
            var residentPolicy = new AccessPolicy(RoleName.RESIDENT);
            residentPolicy.grantPermission(new PermissionCode("CAN_UNLOCK"));
            residentPolicy.grantPermission(new PermissionCode("CAN_LOCK"));
            residentPolicy.grantPermission(new PermissionCode("CAN_TOGGLE_MEDIA"));
            policyRepository.save(residentPolicy);

            var doormanPolicy = new AccessPolicy(RoleName.DOORMAN);
            doormanPolicy.grantPermission(new PermissionCode("CAN_UNLOCK"));
            doormanPolicy.grantPermission(new PermissionCode("CAN_LOCK"));
            doormanPolicy.grantPermission(new PermissionCode("CAN_TOGGLE_MEDIA"));
            doormanPolicy.grantPermission(new PermissionCode("CAN_AUTHORIZE"));
            policyRepository.save(doormanPolicy);
        }

        if (deviceRepository.count() == 0) {
            deviceRepository.save(new IoTDevice("DEV-ESP32-DOOR01"));
        }
    }

    @Override
    public boolean handle(EvaluateAccessCommand command) {
        var policy = policyRepository.findByRole(command.role())
                .orElseThrow(() -> new IllegalArgumentException("Access policy for role not found"));
        return policy.canExecute(new PermissionCode(command.permissionCode()));
    }

    @Override
    public Optional<DoorCommand> handle(DispatchDoorCommand command) {
        var doorCmd = new DoorCommand(command.commandType());
        var savedCmd = doorCommandRepository.save(doorCmd);

        boolean success = commandGateway.send(savedCmd);
        if (success) {
            savedCmd.markDispatched();
            savedCmd.markConfirmed();
        } else {
            savedCmd.markFailed();
        }
        return Optional.of(doorCommandRepository.save(savedCmd));
    }

    @Override
    public Optional<IoTDevice> handle(ToggleDeviceMediaStreamCommand command) {
        var device = deviceRepository.findByDeviceCode(command.deviceCode())
                .orElseThrow(() -> new IllegalArgumentException("IoT Device not found"));
        device.toggleMedia(command.camera(), command.microphone());
        var saved = deviceRepository.save(device);
        commandGateway.sendMediaToggle(command.camera(), command.microphone());
        return Optional.of(saved);
    }

    @Override
    public Optional<SecurityAlarm> handle(TriggerTamperingAlarmCommand command) {
        SensorType type = SensorType.valueOf(command.sensorType());
        var alarm = new SecurityAlarm(type);
        return Optional.of(alarmRepository.save(alarm));
    }

    @Override
    public void handle(ProcessMotionDetectionCommand command) {
        var alarm = new SecurityAlarm(SensorType.ULTRASONIC);
        alarm.acknowledge();
        alarmRepository.save(alarm);
    }

    @Override
    public Optional<IoTDevice> handle(UpdateDoorStateCommand command) {
        var device = deviceRepository.findByDeviceCode("DEV-ESP32-DOOR01")
                .or(() -> deviceRepository.findAll().stream().findFirst())
                .orElseGet(() -> deviceRepository.save(new IoTDevice("DEV-ESP32-DOOR01")));
        device.updateDoorState(command.state());
        return Optional.of(deviceRepository.save(device));
    }
}
