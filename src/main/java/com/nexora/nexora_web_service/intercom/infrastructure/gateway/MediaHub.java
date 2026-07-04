package com.nexora.nexora_web_service.intercom.infrastructure.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket Media Hub for live video and audio.
 * Acts as a reverse proxy multiplexer between the Edge service and the Web/App clients.
 *
 * Edge -> Hub -> Clients
 * Clients -> Hub -> Edge
 */
@Component
public class MediaHub extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MediaHub.class);
    private final ObjectMapper mapper = new ObjectMapper();

    // Map: deviceId -> List of WebSocketSessions for the Edge device itself
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> edgeSessions = new ConcurrentHashMap<>();

    // Map: group (e.g. "doorman_nexbell-door-01") -> List of WebSocketSessions for clients (Web/App)
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> clientSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[MediaHub] New WebSocket connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[MediaHub] WebSocket connection closed: {}", session.getId());
        removeSessionFromAllGroups(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload;
        try {
            payload = mapper.readTree(message.getPayload());
        } catch (Exception e) {
            log.warn("[MediaHub] Ignoring invalid JSON message");
            return;
        }

        String type = payload.has("type") ? payload.get("type").asText() : "";
        String deviceId = payload.has("deviceId") ? payload.get("deviceId").asText() : "";

        switch (type) {
            case "registerEdge":
                registerEdge(deviceId, session);
                break;
            case "subscribeToVideo":
                String groupType = payload.has("groupType") ? payload.get("groupType").asText() : "";
                subscribeClient(deviceId, groupType, session);
                break;
            case "publishFrame":
                String frameData = payload.has("data") ? payload.get("data").asText() : "";
                broadcastToAllClients(deviceId, "receiveFrame", frameData);
                break;
            case "publishAudio":
                String audioGroup = payload.has("targetGroup") ? payload.get("targetGroup").asText() : "";
                String audioData = payload.has("data") ? payload.get("data").asText() : "";
                broadcastToGroup(deviceId, audioGroup, "receiveDeviceAudio", audioData);
                break;
            case "sendAudioToDevice":
                String clientAudioData = payload.has("data") ? payload.get("data").asText() : "";
                sendToEdge(deviceId, "receiveAudioFromCloud", clientAudioData);
                break;
            case "notifyButton":
                String buttonType = payload.has("buttonType") ? payload.get("buttonType").asText() : "";
                log.info("[MediaHub] Button pressed on {}: {}", deviceId, buttonType);
                // The existing SSE (queue-update) and Push notifications (FCM) are already
                // triggered by the REST API when the Edge creates a visit request.
                // We can broadcast a simple event here if needed by the clients.
                break;
            default:
                log.debug("[MediaHub] Unknown message type: {}", type);
        }
    }

    private void registerEdge(String deviceId, WebSocketSession session) {
        if (deviceId.isEmpty()) return;
        edgeSessions.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(session);
        log.info("[MediaHub] Edge registered for deviceId: {}", deviceId);
    }

    private void subscribeClient(String deviceId, String groupType, WebSocketSession session) {
        if (deviceId.isEmpty() || groupType.isEmpty()) return;
        String groupKey = groupType + "_" + deviceId;
        clientSessions.computeIfAbsent(groupKey, k -> new CopyOnWriteArrayList<>()).add(session);
        log.info("[MediaHub] Client subscribed to group: {}", groupKey);
    }

    private void broadcastToGroup(String deviceId, String groupType, String eventType, String data) {
        if (deviceId.isEmpty() || groupType.isEmpty() || data.isEmpty()) return;
        String groupKey = groupType + "_" + deviceId;
        CopyOnWriteArrayList<WebSocketSession> sessions = clientSessions.get(groupKey);
        
        if (sessions == null || sessions.isEmpty()) return;

        ObjectNode msg = mapper.createObjectNode();
        msg.put("type", eventType);
        msg.put("data", data);

        String json;
        try {
            json = mapper.writeValueAsString(msg);
        } catch (Exception e) {
            return;
        }

        TextMessage tm = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(tm);
                } catch (IOException e) {
                    sessions.remove(s);
                }
            } else {
                sessions.remove(s);
            }
        }
    }

    private void broadcastToAllClients(String deviceId, String eventType, String data) {
        if (deviceId.isEmpty() || data.isEmpty()) return;
        String doormanKey = "doorman_" + deviceId;
        String residentKey = "resident_" + deviceId;
        
        broadcastToGroup(deviceId, "doorman", eventType, data);
        broadcastToGroup(deviceId, "resident", eventType, data);
    }

    private void sendToEdge(String deviceId, String eventType, String data) {
        if (deviceId.isEmpty() || data.isEmpty()) return;
        CopyOnWriteArrayList<WebSocketSession> sessions = edgeSessions.get(deviceId);
        
        if (sessions == null || sessions.isEmpty()) return;

        ObjectNode msg = mapper.createObjectNode();
        msg.put("type", eventType);
        msg.put("data", data);

        String json;
        try {
            json = mapper.writeValueAsString(msg);
        } catch (Exception e) {
            return;
        }

        TextMessage tm = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(tm);
                } catch (IOException e) {
                    sessions.remove(s);
                }
            } else {
                sessions.remove(s);
            }
        }
    }

    private void removeSessionFromAllGroups(WebSocketSession session) {
        edgeSessions.values().forEach(list -> list.remove(session));
        clientSessions.values().forEach(list -> list.remove(session));
    }
}
