package com.nexora.nexora_web_service.security.infrastructure.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Forwards face-AI commands (ENROLL_FACE / DELETE_FACES / FACE_ON / FACE_OFF)
 * from the backend to the Edge service, which publishes them to MQTT for the
 * ESP32. Same path the camera/door commands already use.
 */
@Component
public class FaceCommandGateway {

    private static final Logger log = LoggerFactory.getLogger(FaceCommandGateway.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${nexbell.edge-service.host:localhost}")
    private String edgeServiceHost;

    @Value("${nexbell.edge-service.port:3100}")
    private int edgeServicePort;

    // Si se define (p.ej. una URL de túnel ngrok cuando el backend está en la
    // nube), se usa tal cual. Si queda vacía, se arma http://host:puerto.
    @Value("${nexbell.edge-service.base-url:}")
    private String edgeServiceBaseUrl;

    private String edgeUrl(String path) {
        String base = (edgeServiceBaseUrl != null && !edgeServiceBaseUrl.isBlank())
                ? edgeServiceBaseUrl.replaceAll("/+$", "")
                : String.format("http://%s:%d", edgeServiceHost, edgeServicePort);
        return base + path;
    }

    /** Sends a face-AI action to the ESP32 (via the edge). Returns false if the edge is unreachable. */
    public boolean send(String action) {
        String url = edgeUrl("/api/commands/capture");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("action", action), headers);
            restTemplate.postForEntity(url, request, String.class);
            log.info("[FaceGateway] Face command '{}' forwarded to edge.", action);
            return true;
        } catch (RestClientException e) {
            log.error("[FaceGateway] Failed to reach edge at {}: {}", url, e.getMessage());
            return false;
        }
    }
}
