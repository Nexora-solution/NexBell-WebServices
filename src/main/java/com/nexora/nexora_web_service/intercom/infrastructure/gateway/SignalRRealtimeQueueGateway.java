package com.nexora.nexora_web_service.intercom.infrastructure.gateway;

import com.nexora.nexora_web_service.intercom.domain.model.entities.PreRegisteredVisit;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.services.RealtimeQueueGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SignalRRealtimeQueueGateway implements RealtimeQueueGateway {

    private static final Logger log = LoggerFactory.getLogger(SignalRRealtimeQueueGateway.class);

    private static final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());

    public static void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
    }

    @Override
    public void publishQueueUpdate(VisitRequest request) {
        log.info("Publishing real-time queue update for VisitRequest ID '{}' (Status: '{}').", request.getId(), request.getStatus());
        
        synchronized (emitters) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (var emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("queue-update")
                            .data(request));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }
            emitters.removeAll(deadEmitters);
        }
    }

    @Override
    public void publishPreRegisteredUpdate(PreRegisteredVisit visit) {
        log.info("Publishing real-time pre-registered visit update for ID '{}' (Status: '{}').", visit.getId(), visit.getStatus());

        synchronized (emitters) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (var emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("prereg-update")
                            .data(visit));
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }
            emitters.removeAll(deadEmitters);
        }
    }
}
