package com.nexora.nexora_web_service.security.infrastructure.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pushes face-recognition events to subscribed doorman-web clients over SSE:
 * a recognized resident arriving, an unknown face, or a successful enrollment.
 * Mirrors the security-alarm broadcaster.
 */
@Component
public class SseFaceBroadcastGateway {

    private static final Logger log = LoggerFactory.getLogger(SseFaceBroadcastGateway.class);

    private static final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());

    public static void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
    }

    /** Broadcasts a face event (event name + arbitrary data) to all connected clients. */
    public void broadcast(String eventName, Map<String, Object> data) {
        log.info("Broadcasting face event '{}': {}", eventName, data);
        synchronized (emitters) {
            List<SseEmitter> dead = new ArrayList<>();
            for (var emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                } catch (IOException e) {
                    dead.add(emitter);
                }
            }
            emitters.removeAll(dead);
        }
    }
}
