package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.intercom.domain.model.entities.IntercomQueueItem;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetPendingQueueQuery;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetVisitDetailQuery;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomQueryService;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.VisitRequestDetailResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.VisitRequestRepository;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.PreRegisteredVisitRepository;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.EnrichedQueueItemResource;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.VisitStreamResource;
import com.nexora.nexora_web_service.intercom.infrastructure.gateway.SignalRRealtimeQueueGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import com.nexora.nexora_web_service.intercom.infrastructure.mqtt.MqttVideoSubscriber;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/intercom")
@Tag(name = "Intercom Queue", description = "Operational queue administration for doormen")
public class IntercomQueueController {

    private static final Logger log = LoggerFactory.getLogger(IntercomQueueController.class);

    private final IntercomQueryService queryService;
    private final VisitRequestRepository visitRequestRepository;
    private final ApartmentRepository apartmentRepository;
    private final PreRegisteredVisitRepository preRegisteredVisitRepository;
    private final MqttVideoSubscriber mqttVideoSubscriber;

    public IntercomQueueController(IntercomQueryService queryService,
                                   VisitRequestRepository visitRequestRepository,
                                   ApartmentRepository apartmentRepository,
                                   PreRegisteredVisitRepository preRegisteredVisitRepository,
                                   MqttVideoSubscriber mqttVideoSubscriber) {
        this.queryService = queryService;
        this.visitRequestRepository = visitRequestRepository;
        this.apartmentRepository = apartmentRepository;
        this.preRegisteredVisitRepository = preRegisteredVisitRepository;
        this.mqttVideoSubscriber = mqttVideoSubscriber;
    }

    @GetMapping("/queue/pending")
    @Operation(summary = "Get the list of visit requests currently pending check-in")
    public ResponseEntity<List<EnrichedQueueItemResource>> getPendingQueue() {
        var query = new GetPendingQueueQuery();
        var pendingItems = queryService.handle(query);
        
        var enriched = pendingItems.stream().map(item -> {
            var request = visitRequestRepository.findById(item.getVisitRequestId()).orElse(null);
            if (request == null) return null;
            
            String apartmentCode = "N/A";
            var apartmentOpt = apartmentRepository.findById(request.getApartmentId());
            if (apartmentOpt.isPresent()) {
                apartmentCode = apartmentOpt.get().getCode().code();
            }
            
            String type = "walk-in";
            String dni = "—";
            var preRegs = preRegisteredVisitRepository.findAll();
            for (var pr : preRegs) {
                if (pr.isActive() && pr.getVisitorName().equalsIgnoreCase(request.getVisitorName())) {
                    type = "pre-registered";
                    dni = pr.getVisitorDocument() != null ? pr.getVisitorDocument() : "—";
                    break;
                }
            }
            
            return new EnrichedQueueItemResource(
                item.getId(),
                item.getVisitRequestId(),
                request.getVisitorName(),
                dni,
                apartmentCode,
                type,
                item.getStatus(),
                item.getEnqueuedAt().toString()
            );
        }).filter(java.util.Objects::nonNull).toList();
        
        return ResponseEntity.ok(enriched);
    }

    @GetMapping(value = "/queue/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time intercom queue update events")
    public SseEmitter subscribeToQueueStream() {
        SseEmitter emitter = new SseEmitter(24 * 60 * 60 * 1000L); // 24 hours
        SignalRRealtimeQueueGateway.addEmitter(emitter);
        try {
            emitter.send(SseEmitter.event().name("init").data("Connected to NexBell Intercom Realtime Stream"));
        } catch (IOException e) {
            log.error("Failed to send init SSE event", e);
        }
        return emitter;
    }

    @GetMapping("/visit-requests/{id}/stream")
    @Operation(summary = "Get WebRTC/RTSP stream credentials/URL for a visit request")
    public ResponseEntity<VisitStreamResource> getVisitStream(@PathVariable Long id) {
        var streamInfo = new VisitStreamResource(
                id,
                "/api/intercom/video-stream",
                "HTTP_MJPEG",
                "stream-" + id
        );
        return ResponseEntity.ok(streamInfo);
    }

    @GetMapping("/video-stream/status")
    @Operation(summary = "Check if the camera is actively streaming frames right now")
    public ResponseEntity<java.util.Map<String, Boolean>> getVideoStreamStatus() {
        return ResponseEntity.ok(java.util.Map.of("live", mqttVideoSubscriber.isLive()));
    }

    @GetMapping("/video-stream")
    @Operation(summary = "Get the actual MJPEG continuous video stream")
    public void getMjpegStream(HttpServletResponse response) {
        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        try {
            var out = response.getOutputStream();
            while (true) {
                byte[] frame = mqttVideoSubscriber.getLatestFrame();
                if (frame != null && frame.length > 0) {
                    out.write(("--frame\r\n").getBytes());
                    out.write(("Content-Type: image/jpeg\r\n").getBytes());
                    out.write(("Content-Length: " + frame.length + "\r\n\r\n").getBytes());
                    out.write(frame);
                    out.write(("\r\n").getBytes());
                    out.flush();
                }
                Thread.sleep(50); // Target ~20 FPS
            }
        } catch (Exception e) {
            log.info("Video stream client disconnected.");
        }
    }

    @GetMapping("/visit-requests/{id}")
    @Operation(summary = "Get the details of a specific visit request by its ID")
    public ResponseEntity<VisitRequestDetailResource> getVisitDetail(@PathVariable Long id) {
        var query = new GetVisitDetailQuery(id);
        var requestOpt = queryService.handle(query);
        return requestOpt.map(req -> ResponseEntity.ok(toDetailResource(req)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private VisitRequestDetailResource toDetailResource(VisitRequest req) {
        String photoUrl = req.getEvidence() != null ? req.getEvidence().getPhotoUrl().uri() : null;
        String audioUrl = req.getEvidence() != null && req.getEvidence().getAudioUrl() != null ? req.getEvidence().getAudioUrl().uri() : null;
        return new VisitRequestDetailResource(
                req.getId(),
                req.getVisitorName(),
                req.getApartmentId(),
                req.getStatus(),
                photoUrl,
                audioUrl,
                req.getCreatedAt()
        );
    }
}
