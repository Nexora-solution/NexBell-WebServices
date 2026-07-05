package com.nexora.nexora_web_service.security.application.internal.commandservices;

import com.nexora.nexora_web_service.audit.domain.model.entities.AccessTimelineEntry;
import com.nexora.nexora_web_service.audit.infrastructure.persistence.jpa.repositories.AccessTimelineEntryRepository;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.security.domain.model.commands.*;
import com.nexora.nexora_web_service.security.domain.model.entities.AccessPolicy;
import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.PermissionCode;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.SensorType;
import com.nexora.nexora_web_service.security.domain.services.AlarmBroadcastGateway;
import com.nexora.nexora_web_service.security.domain.services.IoTCommandGateway;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.AccessPolicyRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.DoorCommandRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.IoTDeviceRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.SecurityAlarmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityCommandServiceImplTest {

    @Mock AccessPolicyRepository policyRepository;
    @Mock DoorCommandRepository doorCommandRepository;
    @Mock IoTDeviceRepository deviceRepository;
    @Mock SecurityAlarmRepository alarmRepository;
    @Mock IoTCommandGateway commandGateway;
    @Mock AlarmBroadcastGateway alarmBroadcastGateway;
    @Mock AccessTimelineEntryRepository timelineRepository;

    @InjectMocks SecurityCommandServiceImpl service;

    @Test
    void initDefaultPolicies_whenDatabaseIsEmpty_savesDefaultPoliciesAndDevice() {
        when(policyRepository.count()).thenReturn(0L);
        when(deviceRepository.count()).thenReturn(0L);

        service.initDefaultPolicies();

        verify(policyRepository, times(2)).save(any(AccessPolicy.class));
        verify(deviceRepository).save(any(IoTDevice.class));
    }

    @Test
    void initDefaultPolicies_whenDatabaseIsNotEmpty_doesNotSaveAnything() {
        when(policyRepository.count()).thenReturn(2L);
        when(deviceRepository.count()).thenReturn(1L);

        service.initDefaultPolicies();

        verify(policyRepository, never()).save(any(AccessPolicy.class));
        verify(deviceRepository, never()).save(any(IoTDevice.class));
    }

    @Test
    void handle_evaluateAccessCommand_returnsTrueIfPermitted() {
        var policy = new AccessPolicy(RoleName.RESIDENT);
        policy.grantPermission(new PermissionCode("CAN_UNLOCK"));
        when(policyRepository.findByRole(RoleName.RESIDENT)).thenReturn(Optional.of(policy));

        boolean permitted = service.handle(new EvaluateAccessCommand(RoleName.RESIDENT, "CAN_UNLOCK"));

        assertTrue(permitted);
    }

    @Test
    void handle_evaluateAccessCommand_returnsFalseIfNotPermitted() {
        var policy = new AccessPolicy(RoleName.RESIDENT);
        when(policyRepository.findByRole(RoleName.RESIDENT)).thenReturn(Optional.of(policy));

        boolean permitted = service.handle(new EvaluateAccessCommand(RoleName.RESIDENT, "CAN_UNLOCK"));

        assertFalse(permitted);
    }

    @Test
    void handle_dispatchDoorCommand_whenGatewaySucceeds_marksDispatchedAndConfirmed() {
        var doorCmd = new DoorCommand(CommandType.UNLOCK);
        when(doorCommandRepository.save(any(DoorCommand.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandGateway.send(any(DoorCommand.class))).thenReturn(true);

        var result = service.handle(new DispatchDoorCommand(CommandType.UNLOCK));

        assertTrue(result.isPresent());
        assertEquals("CONFIRMED", result.get().getStatus());
        verify(doorCommandRepository, times(2)).save(any(DoorCommand.class));
    }

    @Test
    void handle_dispatchDoorCommand_whenGatewayFails_marksFailed() {
        var doorCmd = new DoorCommand(CommandType.UNLOCK);
        when(doorCommandRepository.save(any(DoorCommand.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandGateway.send(any(DoorCommand.class))).thenReturn(false);

        var result = service.handle(new DispatchDoorCommand(CommandType.UNLOCK));

        assertTrue(result.isPresent());
        assertEquals("FAILED", result.get().getStatus());
        verify(doorCommandRepository, times(2)).save(any(DoorCommand.class));
    }

    @Test
    void handle_toggleDeviceMediaStreamCommand_togglesMediaState() {
        var device = new IoTDevice("DEV-ESP32-DOOR01");
        when(deviceRepository.findByDeviceCode("DEV-ESP32-DOOR01")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(IoTDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandGateway.sendMediaToggle(true, false)).thenReturn(true);

        var result = service.handle(new ToggleDeviceMediaStreamCommand("DEV-ESP32-DOOR01", true, false));

        assertTrue(result.isPresent());
        assertTrue(result.get().isCameraEnabled());
        assertFalse(result.get().isMicrophoneEnabled());
        verify(commandGateway).sendMediaToggle(true, false);
    }

    @Test
    void handle_triggerTamperingAlarmCommand_savesAlarmAndTimelineEntry() {
        when(alarmRepository.save(any(SecurityAlarm.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(timelineRepository.save(any(AccessTimelineEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new TriggerTamperingAlarmCommand("SW420_VIBRATION"));

        assertTrue(result.isPresent());
        assertEquals(SensorType.SW420_VIBRATION, result.get().getSensorType());
        verify(alarmBroadcastGateway).publishAlarm(any(SecurityAlarm.class));
        verify(timelineRepository).save(any(AccessTimelineEntry.class));
    }

    @Test
    void handle_processMotionDetectionCommand_savesAcknowledgedUltrasonicAlarm() {
        when(alarmRepository.save(any(SecurityAlarm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new ProcessMotionDetectionCommand());

        verify(alarmRepository).save(any(SecurityAlarm.class));
    }

    @Test
    void handle_updateDoorStateCommand_whenStateChanges_savesTimelineEntry() {
        var device = new IoTDevice("DEV-ESP32-DOOR01");
        device.updateDoorState("CLOSED");
        when(deviceRepository.findByDeviceCode("DEV-ESP32-DOOR01")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(IoTDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new UpdateDoorStateCommand("OPEN"));

        assertTrue(result.isPresent());
        assertEquals("OPEN", result.get().getDoorState());
        verify(timelineRepository).save(any(AccessTimelineEntry.class));
    }

    @Test
    void handle_updateDoorStateCommand_whenStateUnchanged_doesNotSaveTimelineEntry() {
        var device = new IoTDevice("DEV-ESP32-DOOR01");
        device.updateDoorState("CLOSED");
        when(deviceRepository.findByDeviceCode("DEV-ESP32-DOOR01")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(IoTDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new UpdateDoorStateCommand("CLOSED"));

        assertTrue(result.isPresent());
        assertEquals("CLOSED", result.get().getDoorState());
        verify(timelineRepository, never()).save(any(AccessTimelineEntry.class));
    }
}
