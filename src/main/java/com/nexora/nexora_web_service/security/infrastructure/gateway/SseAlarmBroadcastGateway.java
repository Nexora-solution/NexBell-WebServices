package com.nexora.nexora_web_service.security.infrastructure.gateway;

import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import com.nexora.nexora_web_service.security.domain.services.AlarmBroadcastGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Broadcasts security alarms (e.g. door tampering / forced-entry) to all
 * subscribed doorman-web clients over Server-Sent Events. Mirrors the
 * realtime-queue gateway pattern used by the intercom context, but kept
 * inside the security context so the two stay decoupled.
 */
@Component
public class SseAlarmBroadcastGateway implements AlarmBroadcastGateway {

    private static final Logger log = LoggerFactory.getLogger(SseAlarmBroadcastGateway.class);

    private static final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());

    public static void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
    }

    @Override
    public void publishAlarm(SecurityAlarm alarm) {
        log.info("Broadcasting security alarm ID '{}' (sensor: '{}', status: '{}').",
                alarm.getId(), alarm.getSensorType(), alarm.getStatus());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", alarm.getId());
        payload.put("sensorType", alarm.getSensorType().name());
        payload.put("status", alarm.getStatus());
        payload.put("triggeredAt", alarm.getTriggeredAt() != null ? alarm.getTriggeredAt().toString() : null);

        synchronized (emitters) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (var emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("alarm").data(payload));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }
            emitters.removeAll(deadEmitters);
        }
    }
}
