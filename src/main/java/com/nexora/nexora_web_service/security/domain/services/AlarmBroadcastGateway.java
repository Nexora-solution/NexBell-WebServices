package com.nexora.nexora_web_service.security.domain.services;

import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;

/**
 * Outbound port for pushing security alarms to subscribed clients in real time
 * (e.g. the doorman web app). Implemented over Server-Sent Events.
 */
public interface AlarmBroadcastGateway {
    void publishAlarm(SecurityAlarm alarm);
}
