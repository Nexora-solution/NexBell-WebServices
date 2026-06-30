package com.nexora.nexora_web_service.security.application.internal.commandservices;

import com.nexora.nexora_web_service.audit.domain.model.entities.AccessTimelineEntry;
import com.nexora.nexora_web_service.audit.infrastructure.persistence.jpa.repositories.AccessTimelineEntryRepository;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.security.domain.model.commands.*;
import com.nexora.nexora_web_service.security.domain.model.entities.AccessPolicy;
import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.PermissionCode;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.SensorType;
import com.nexora.nexora_web_service.security.domain.services.AlarmBroadcastGateway;
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
    private final AlarmBroadcastGateway alarmBroadcastGateway;
    private final AccessTimelineEntryRepository timelineRepository;

    public SecurityCommandServiceImpl(AccessPolicyRepository policyRepository,
                                       DoorCommandRepository doorCommandRepository,
                                       IoTDeviceRepository deviceRepository,
                                       SecurityAlarmRepository alarmRepository,
                                       IoTCommandGateway commandGateway,
                                       AlarmBroadcastGateway alarmBroadcastGateway,
                                       AccessTimelineEntryRepository timelineRepository) {
        this.policyRepository = policyRepository;
        this.doorCommandRepository = doorCommandRepository;
        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
        this.commandGateway = commandGateway;
        this.alarmBroadcastGateway = alarmBroadcastGateway;
        this.timelineRepository = timelineRepository;
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
        var saved = alarmRepository.save(alarm);
        alarmBroadcastGateway.publishAlarm(saved); // push to the doorman web in real time (SSE)
        // Record it in the activity feed (recent activity on the dashboard).
        timelineRepository.save(new AccessTimelineEntry("Movimiento detectado en la puerta"));
        return Optional.of(saved);
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
        String oldState = device.getDoorState();
        device.updateDoorState(command.state());
        var saved = deviceRepository.save(device);
        // Only record a hardware-log entry when the physical state actually changes,
        // so the activity feed shows a clean open/close history (not every reading).
        String newState = saved.getDoorState();
        if (newState != null && !newState.equalsIgnoreCase(oldState)) {
            String desc = "OPEN".equalsIgnoreCase(newState) ? "La puerta se abrió" : "La puerta se cerró";
            timelineRepository.save(new AccessTimelineEntry(desc));
        }
        return Optional.of(saved);
    }
}
