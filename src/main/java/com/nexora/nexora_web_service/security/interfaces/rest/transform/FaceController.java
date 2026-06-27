package com.nexora.nexora_web_service.security.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.security.domain.model.entities.ResidentFace;
import com.nexora.nexora_web_service.security.infrastructure.gateway.FaceCommandGateway;
import com.nexora.nexora_web_service.security.infrastructure.gateway.SseFaceBroadcastGateway;
import com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories.ResidentFaceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Face recognition for RESIDENTS. Enrollment is triggered by the doorman from
 * the web (the resident stands in front of the ESP32 camera); recognition fires
 * when an enrolled resident later arrives, and the doorman is notified.
 * The ESP32 does the AI on-device; here we only map face IDs to residents.
 */
@RestController
@RequestMapping("/api/security/face")
@Tag(name = "Face Recognition", description = "Resident on-device face enrollment & recognition")
public class FaceController {

    private static final Logger log = LoggerFactory.getLogger(FaceController.class);

    private final FaceCommandGateway commandGateway;
    private final SseFaceBroadcastGateway faceBroadcast;
    private final ResidentFaceRepository residentFaceRepository;
    private final ResidentDirectoryRepository residentDirectoryRepository;
    private final ApartmentRepository apartmentRepository;

    // Which resident the NEXT enrolled face belongs to (set when the doorman
    // presses "register face", consumed when the ESP32 reports face_enrolled).
    private volatile Long pendingResidentId = null;

    public FaceController(FaceCommandGateway commandGateway,
                          SseFaceBroadcastGateway faceBroadcast,
                          ResidentFaceRepository residentFaceRepository,
                          ResidentDirectoryRepository residentDirectoryRepository,
                          ApartmentRepository apartmentRepository) {
        this.commandGateway = commandGateway;
        this.faceBroadcast = faceBroadcast;
        this.residentFaceRepository = residentFaceRepository;
        this.residentDirectoryRepository = residentDirectoryRepository;
        this.apartmentRepository = apartmentRepository;
    }

    public record EnrollRequest(Long residentId) {}
    public record RecognitionRequest(boolean on) {}

    @PostMapping("/enroll")
    @Operation(summary = "Register the next face the camera sees as this resident")
    public ResponseEntity<?> enroll(@RequestBody EnrollRequest request) {
        if (request.residentId() == null) return ResponseEntity.badRequest().body("residentId is required");
        if (residentDirectoryRepository.findById(request.residentId()).isEmpty()) {
            return ResponseEntity.status(404).body("Resident not found");
        }
        this.pendingResidentId = request.residentId();
        boolean ok = commandGateway.send("ENROLL_FACE");
        if (!ok) return ResponseEntity.status(502).body("Could not reach the IoT device (edge)");
        return ResponseEntity.ok(Map.of("status", "ENROLLING", "residentId", request.residentId()));
    }

    @PostMapping("/recognition")
    @Operation(summary = "Turn the camera's face recognition on/off")
    public ResponseEntity<?> recognition(@RequestBody RecognitionRequest request) {
        boolean ok = commandGateway.send(request.on() ? "FACE_ON" : "FACE_OFF");
        if (!ok) return ResponseEntity.status(502).body("Could not reach the IoT device (edge)");
        return ResponseEntity.ok(Map.of("recognition", request.on()));
    }

    @PostMapping("/clear")
    @Operation(summary = "Delete all enrolled faces (device + associations)")
    public ResponseEntity<?> clear() {
        commandGateway.send("DELETE_FACES");
        residentFaceRepository.deleteAll();
        this.pendingResidentId = null;
        return ResponseEntity.ok(Map.of("status", "CLEARED"));
    }

    @GetMapping("/residents")
    @Operation(summary = "List residents that have a face enrolled")
    public ResponseEntity<List<Map<String, Object>>> enrolledResidents() {
        var list = residentFaceRepository.findAll().stream()
                .map(rf -> {
                    Map<String, Object> m = residentData(rf.getResidentId());
                    m.put("faceId", rf.getFaceId());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/event")
    @Operation(summary = "Ingest a face-recognition event from the IoT device (called by the edge)")
    public ResponseEntity<Void> handleEvent(@RequestBody Map<String, Object> event) {
        String type = (String) event.get("event");
        if (type == null) return ResponseEntity.badRequest().build();

        switch (type) {
            case "face_enrolled" -> {
                Integer faceId = toInt(event.get("id"));
                Long residentId = this.pendingResidentId;
                if (faceId != null && residentId != null) {
                    // One face per resident: replace any previous association.
                    residentFaceRepository.findByResidentId(residentId)
                            .ifPresent(residentFaceRepository::delete);
                    residentFaceRepository.save(new ResidentFace(faceId, residentId));
                    this.pendingResidentId = null;
                    Map<String, Object> data = residentData(residentId);
                    data.put("faceId", faceId);
                    faceBroadcast.broadcast("face-enrolled", data);
                    log.info("Face {} enrolled for resident {}", faceId, residentId);
                }
            }
            case "face_recognized" -> {
                Integer faceId = toInt(event.get("id"));
                if (faceId != null) {
                    residentFaceRepository.findByFaceId(faceId).ifPresent(rf -> {
                        Map<String, Object> data = residentData(rf.getResidentId());
                        data.put("faceId", faceId);
                        data.put("similarity", event.get("similarity"));
                        faceBroadcast.broadcast("face-recognized", data);
                    });
                }
            }
            case "face_unknown" -> faceBroadcast.broadcast("face-unknown", Map.of());
            case "face_detected" -> {
                Integer count = toInt(event.get("count"));
                faceBroadcast.broadcast("face-detected", Map.of("count", count != null ? count : 1));
            }
            default -> { /* faces_deleted / enroll_failed / etc. — no doorman action */ }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time face events (recognized resident, enrollment, unknown)")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(24 * 60 * 60 * 1000L);
        SseFaceBroadcastGateway.addEmitter(emitter);
        try {
            emitter.send(SseEmitter.event().name("init").data("Connected to NexBell Face Stream"));
        } catch (IOException e) {
            log.error("Failed to send init SSE event", e);
        }
        return emitter;
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private Map<String, Object> residentData(Long residentId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("residentId", residentId);
        String name = "";
        String apartmentCode = "";
        var profileOpt = residentDirectoryRepository.findById(residentId);
        if (profileOpt.isPresent()) {
            name = profileOpt.get().getFullName();
            var apts = apartmentRepository.findByResidentId(residentId);
            if (!apts.isEmpty()) {
                apartmentCode = apts.get(apts.size() - 1).getCode().code();
            }
        }
        m.put("residentName", name);
        m.put("apartmentCode", apartmentCode);
        return m;
    }

    private static Integer toInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}
