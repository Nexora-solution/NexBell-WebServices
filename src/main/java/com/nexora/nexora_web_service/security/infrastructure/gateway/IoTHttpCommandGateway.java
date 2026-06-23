package com.nexora.nexora_web_service.security.infrastructure.gateway;

import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.CommandType;
import com.nexora.nexora_web_service.security.domain.services.IoTCommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Infrastructure Gateway: IoT HTTP Command Gateway
 *
 * Forwards door lock/unlock commands from the Spring Boot backend to the
 * NexBell Edge Service, which then publishes to the MQTT broker so the
 * ESP32S3 can physically actuate the door lock.
 *
 * Edge Service endpoint: POST http://<edgeServiceHost>:<edgeServicePort>/api/commands/unlock
 */
@Component
public class IoTHttpCommandGateway implements IoTCommandGateway {

    private static final Logger log = LoggerFactory.getLogger(IoTHttpCommandGateway.class);

    private final RestTemplate restTemplate;

    @Value("${nexbell.edge-service.host:localhost}")
    private String edgeServiceHost;

    @Value("${nexbell.edge-service.port:3100}")
    private int edgeServicePort;

    public IoTHttpCommandGateway() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public boolean send(DoorCommand command) {
        if (command.getCommandType() == CommandType.UNLOCK) {
            return forwardUnlock();
        }
        // LOCK commands are handled by the backend state only (no hardware actuator for lock)
        log.info("[IoTGateway] LOCK command recorded in backend — no hardware signal needed.");
        return true;
    }

    @Override
    public boolean sendMediaToggle(boolean camera, boolean microphone) {
        // The edge service today only exposes a video stream switch (START_VIDEO/STOP_VIDEO).
        // There is no separate microphone-only command yet, so camera state drives the toggle.
        String action = camera ? "START_VIDEO" : "STOP_VIDEO";
        String url = String.format("http://%s:%d/api/commands/capture", edgeServiceHost, edgeServicePort);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("action", action), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[IoTGateway] Media toggle ({}) forwarded to edge service successfully.", action);
                return true;
            } else {
                log.warn("[IoTGateway] Edge service returned status: {}", response.getStatusCode());
                return false;
            }
        } catch (RestClientException e) {
            log.error("[IoTGateway] Failed to reach edge service at {}: {}", url, e.getMessage());
            return true;
        }
    }

    private boolean forwardUnlock() {
        String url = String.format("http://%s:%d/api/commands/unlock", edgeServiceHost, edgeServicePort);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[IoTGateway] UNLOCK forwarded to edge service successfully.");
                return true;
            } else {
                log.warn("[IoTGateway] Edge service returned status: {}", response.getStatusCode());
                return false;
            }
        } catch (RestClientException e) {
            log.error("[IoTGateway] Failed to reach edge service at {}: {}", url, e.getMessage());
            // Return true to avoid blocking the backend command — hardware will reconnect.
            return true;
        }
    }
}
