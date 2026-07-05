package com.nexora.nexora_web_service.intercom.application.internal.commandservices;

import com.nexora.nexora_web_service.intercom.application.internal.outboundservices.acl.ExternalDirectoryService;
import com.nexora.nexora_web_service.intercom.domain.model.commands.*;
import com.nexora.nexora_web_service.intercom.domain.model.entities.*;
import com.nexora.nexora_web_service.intercom.domain.services.NotificationGateway;
import com.nexora.nexora_web_service.intercom.domain.services.RealtimeQueueGateway;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntercomCommandServiceImplTest {

    @Mock VisitRequestRepository visitRequestRepository;
    @Mock VisitorEvidenceRepository evidenceRepository;
    @Mock NotificationDispatchRepository notificationRepository;
    @Mock IntercomQueueItemRepository queueRepository;
    @Mock PreRegisteredVisitRepository preRegisteredVisitRepository;
    @Mock ExternalDirectoryService directoryService;
    @Mock NotificationGateway notificationGateway;
    @Mock RealtimeQueueGateway realtimeQueueGateway;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks IntercomCommandServiceImpl service;

    @Test
    void handle_createVisitRequest_whenApartmentDoesNotExist_throwsException() {
        when(directoryService.existsApartment(101L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                service.handle(new CreateVisitRequestCommand("Juan Perez", 101L))
        );
    }

    @Test
    void handle_createVisitRequest_whenApartmentExists_withFcmToken_sendsNotificationAndPublishesQueue() {
        when(directoryService.existsApartment(101L)).thenReturn(true);
        when(directoryService.fetchResidentFcmTokenByApartment(101L)).thenReturn(Optional.of("fcm-token-123"));
        when(visitRequestRepository.save(any(VisitRequest.class))).thenAnswer(invocation -> {
            var req = (VisitRequest) invocation.getArgument(0);
            return req;
        });
        when(notificationGateway.send(any(), eq("fcm-token-123"), any(), eq("Juan Perez"), eq("VISIT_REQUEST"))).thenReturn(true);

        var result = service.handle(new CreateVisitRequestCommand("Juan Perez", 101L));

        assertTrue(result.isPresent());
        var req = result.get();
        assertEquals("NOTIFIED", req.getStatus());
        assertEquals("DELIVERED", req.getNotification().getStatus());
        verify(queueRepository).save(any(IntercomQueueItem.class));
        verify(realtimeQueueGateway).publishQueueUpdate(req);
    }

    @Test
    void handle_createVisitRequest_whenApartmentExists_withoutFcmToken_marksNotificationFailed() {
        when(directoryService.existsApartment(101L)).thenReturn(true);
        when(directoryService.fetchResidentFcmTokenByApartment(101L)).thenReturn(Optional.empty());
        when(visitRequestRepository.save(any(VisitRequest.class))).thenAnswer(invocation -> (VisitRequest) invocation.getArgument(0));

        var result = service.handle(new CreateVisitRequestCommand("Juan Perez", 101L));

        assertTrue(result.isPresent());
        var req = result.get();
        assertEquals("NOTIFIED", req.getStatus());
        assertEquals("FAILED", req.getNotification().getStatus());
        verify(queueRepository).save(any(IntercomQueueItem.class));
        verify(realtimeQueueGateway).publishQueueUpdate(req);
    }

    @Test
    void handle_attachVisitorEvidence_updatesEvidence() {
        var visitRequest = new VisitRequest("Juan Perez", 101L);
        visitRequest.setId(1L);
        when(visitRequestRepository.findById(1L)).thenReturn(Optional.of(visitRequest));
        when(visitRequestRepository.save(any(VisitRequest.class))).thenAnswer(invocation -> (VisitRequest) invocation.getArgument(0));

        var result = service.handle(new AttachVisitorEvidenceCommand(1L, "http://photo.url", "http://audio.url"));

        assertTrue(result.isPresent());
        assertNotNull(result.get().getEvidence());
        assertEquals("http://photo.url", result.get().getEvidence().getPhotoUrl().uri());
        assertEquals("http://audio.url", result.get().getEvidence().getAudioUrl().uri());
        verify(realtimeQueueGateway).publishQueueUpdate(visitRequest);
    }

    @Test
    void handle_registerAccessDecision_updatesDecisionAndDequeues() {
        var visitRequest = new VisitRequest("Juan Perez", 101L);
        visitRequest.setId(1L);
        var queueItem = new IntercomQueueItem(1L);

        when(visitRequestRepository.findById(1L)).thenReturn(Optional.of(visitRequest));
        when(queueRepository.findAll()).thenReturn(List.of(queueItem));
        when(visitRequestRepository.save(any(VisitRequest.class))).thenAnswer(invocation -> (VisitRequest) invocation.getArgument(0));

        var result = service.handle(new RegisterAccessDecisionCommand(1L, "APPROVED"));

        assertTrue(result.isPresent());
        assertEquals("APPROVED", result.get().getStatus());
        assertEquals("DEQUEUED", queueItem.getStatus());
        verify(queueRepository).save(queueItem);
        verify(realtimeQueueGateway).publishQueueUpdate(visitRequest);
        verify(eventPublisher).publishEvent(visitRequest);
    }

    @Test
    void handle_createPreRegisteredVisit_savesVisit() {
        when(preRegisteredVisitRepository.save(any(PreRegisteredVisit.class))).thenAnswer(invocation -> (PreRegisteredVisit) invocation.getArgument(0));

        var result = service.handle(new CreatePreRegisteredVisitCommand(200L, "Visita Juan", "77777777", "http://photo.url", java.time.LocalDateTime.now(), "RESIDENT"));

        assertTrue(result.isPresent());
        assertEquals("Visita Juan", result.get().getVisitorName());
    }

    @Test
    void handle_updatePreRegisteredVisit_updatesVisit() {
        var visit = new PreRegisteredVisit(200L, "Visita Juan", "77777777", "http://photo.url", java.time.LocalDateTime.now(), "RESIDENT");
        when(preRegisteredVisitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(preRegisteredVisitRepository.save(any(PreRegisteredVisit.class))).thenAnswer(invocation -> (PreRegisteredVisit) invocation.getArgument(0));

        var result = service.handle(new UpdatePreRegisteredVisitCommand(1L, "Visita Pedro", "88888888", "http://photo2.url", java.time.LocalDateTime.now()));

        assertTrue(result.isPresent());
        assertEquals("Visita Pedro", result.get().getVisitorName());
        assertEquals("88888888", result.get().getVisitorDocument());
    }

    @Test
    void handle_cancelPreRegisteredVisit_cancelsVisit() {
        var visit = new PreRegisteredVisit(200L, "Visita Juan", "77777777", "http://photo.url", java.time.LocalDateTime.now(), "RESIDENT");
        when(preRegisteredVisitRepository.findById(1L)).thenReturn(Optional.of(visit));

        service.handle(new CancelPreRegisteredVisitCommand(1L));

        assertFalse(visit.isActive());
        verify(preRegisteredVisitRepository).save(visit);
    }
}
