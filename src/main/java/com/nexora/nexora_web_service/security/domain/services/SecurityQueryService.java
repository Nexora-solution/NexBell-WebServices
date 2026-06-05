package com.nexora.nexora_web_service.security.domain.services;

import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.model.queries.GetActiveAlarmsQuery;
import com.nexora.nexora_web_service.security.domain.model.queries.GetIoTDeviceStatusQuery;

import java.util.List;
import java.util.Optional;

public interface SecurityQueryService {
    List<SecurityAlarm> handle(GetActiveAlarmsQuery query);
    Optional<IoTDevice> handle(GetIoTDeviceStatusQuery query);
}
