package com.nexora.nexora_web_service.security.application.internal.queryservices;

import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.model.queries.GetActiveAlarmsQuery;
import com.nexora.nexora_web_service.security.domain.model.queries.GetIoTDeviceStatusQuery;
import com.nexora.nexora_web_service.security.domain.services.SecurityQueryService;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.IoTDeviceRepository;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.SecurityAlarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SecurityQueryServiceImpl implements SecurityQueryService {

    private final SecurityAlarmRepository alarmRepository;
    private final IoTDeviceRepository deviceRepository;

    public SecurityQueryServiceImpl(SecurityAlarmRepository alarmRepository,
                                     IoTDeviceRepository deviceRepository) {
        this.alarmRepository = alarmRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public List<SecurityAlarm> handle(GetActiveAlarmsQuery query) {
        return alarmRepository.findByStatus("TRIGGERED");
    }

    @Override
    public Optional<IoTDevice> handle(GetIoTDeviceStatusQuery query) {
        return deviceRepository.findByDeviceCode(query.deviceCode());
    }
}
