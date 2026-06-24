package com.nexora.nexora_web_service.intercom.application.internal.commandservices;

import com.nexora.nexora_web_service.intercom.application.internal.outboundservices.acl.ExternalDirectoryService;
import com.nexora.nexora_web_service.intercom.domain.model.commands.*;
import com.nexora.nexora_web_service.intercom.domain.model.entities.*;
import com.nexora.nexora_web_service.intercom.domain.model.valueobjects.EvidenceUri;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.domain.services.NotificationGateway;
import com.nexora.nexora_web_service.intercom.domain.services.RealtimeQueueGateway;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class IntercomCommandServiceImpl implements IntercomCommandService {

    private final VisitRequestRepository visitRequestRepository;
    private final VisitorEvidenceRepository evidenceRepository;
    private final NotificationDispatchRepository notificationRepository;
    private final IntercomQueueItemRepository queueRepository;
    private final PreRegisteredVisitRepository preRegisteredVisitRepository;
    private final ExternalDirectoryService directoryService;
    private final NotificationGateway notificationGateway;
    private final RealtimeQueueGateway realtimeQueueGateway;
    private final ApplicationEventPublisher eventPublisher;

    public IntercomCommandServiceImpl(VisitRequestRepository visitRequestRepository,
                                     VisitorEvidenceRepository evidenceRepository,
                                     NotificationDispatchRepository notificationRepository,
                                     IntercomQueueItemRepository queueRepository,
                                     PreRegisteredVisitRepository preRegisteredVisitRepository,
                                     ExternalDirectoryService directoryService,
                                     NotificationGateway notificationGateway,
                                     RealtimeQueueGateway realtimeQueueGateway,
                                     ApplicationEventPublisher eventPublisher) {
        this.visitRequestRepository = visitRequestRepository;
        this.evidenceRepository = evidenceRepository;
        this.notificationRepository = notificationRepository;
        this.queueRepository = queueRepository;
        this.preRegisteredVisitRepository = preRegisteredVisitRepository;
        this.directoryService = directoryService;
        this.notificationGateway = notificationGateway;
        this.realtimeQueueGateway = realtimeQueueGateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<VisitRequest> handle(CreateVisitRequestCommand command) {
        if (!directoryService.existsApartment(command.apartmentId())) {
            throw new IllegalArgumentException("Target apartment does not exist in directory");
        }

        var visitRequest = new VisitRequest(command.visitorName(), command.apartmentId());
        var savedRequest = visitRequestRepository.save(visitRequest);

        var queueItem = new IntercomQueueItem(savedRequest.getId());
        queueRepository.save(queueItem);

        var notification = new NotificationDispatch("SCHEDULED");
        var residentFcmTokenOpt = directoryService.fetchResidentFcmTokenByApartment(command.apartmentId());
        if (residentFcmTokenOpt.isPresent()) {
            boolean sent = notificationGateway.send(notification, residentFcmTokenOpt.get(), savedRequest.getId(), command.visitorName(), "VISIT_REQUEST");
            if (sent) {
                notification.markSent();
                notification.markDelivered();
            } else {
                notification.markFailed();
            }
        } else {
            // No FCM token registered for this resident's device — the visit still
            // shows up in the doorman queue and the resident's in-app history,
            // just without a push notification reaching their phone.
            notification.markFailed();
        }

        savedRequest.markNotified(notification);
        var finalRequest = visitRequestRepository.save(savedRequest);

        realtimeQueueGateway.publishQueueUpdate(finalRequest);

        return Optional.of(finalRequest);
    }

    @Override
    public Optional<VisitRequest> handle(AttachVisitorEvidenceCommand command) {
        var visitRequest = visitRequestRepository.findById(command.visitRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Visit request not found"));

        var photoUrl = new EvidenceUri(command.photoUrl());
        var audioUrl = command.audioUrl() != null && !command.audioUrl().isBlank() ? new EvidenceUri(command.audioUrl()) : null;

        var evidence = new VisitorEvidence(photoUrl, audioUrl);
        visitRequest.attachEvidence(evidence);
        var savedRequest = visitRequestRepository.save(visitRequest);

        realtimeQueueGateway.publishQueueUpdate(savedRequest);
        return Optional.of(savedRequest);
    }

    @Override
    public Optional<VisitRequest> handle(RegisterAccessDecisionCommand command) {
        var visitRequest = visitRequestRepository.findById(command.visitRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Visit request not found"));

        visitRequest.registerDecision(command.decision().toUpperCase());

        var queueItems = queueRepository.findAll();
        for (var item : queueItems) {
            if (item.getVisitRequestId().equals(visitRequest.getId()) && !"DEQUEUED".equals(item.getStatus())) {
                item.dequeue();
                queueRepository.save(item);
            }
        }
        var savedRequest = visitRequestRepository.save(visitRequest);

        realtimeQueueGateway.publishQueueUpdate(savedRequest);

        eventPublisher.publishEvent(savedRequest);

        return Optional.of(savedRequest);
    }

    @Override
    public Optional<PreRegisteredVisit> handle(CreatePreRegisteredVisitCommand command) {
        var visit = new PreRegisteredVisit(
                command.residentId(),
                command.visitorName(),
                command.visitorDocument(),
                command.expectedAt(),
                command.registeredBy()
        );
        return Optional.of(preRegisteredVisitRepository.save(visit));
    }

    @Override
    public Optional<PreRegisteredVisit> handle(UpdatePreRegisteredVisitCommand command) {
        var visit = preRegisteredVisitRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Pre-registered visit not found"));
        visit.update(command.visitorName(), command.visitorDocument(), command.expectedAt());
        return Optional.of(preRegisteredVisitRepository.save(visit));
    }

    @Override
    public void handle(CancelPreRegisteredVisitCommand command) {
        preRegisteredVisitRepository.findById(command.id()).ifPresent(visit -> {
            visit.cancel();
            preRegisteredVisitRepository.save(visit);
        });
    }

    @Override
    public Optional<PreRegisteredVisit> handle(RegisterPreRegisteredDecisionCommand command) {
        var visit = preRegisteredVisitRepository.findById(command.preRegisteredVisitId())
                .orElseThrow(() -> new IllegalArgumentException("Pre-registered visit not found"));

        visit.registerDecision(command.decision().toUpperCase());
        var savedVisit = preRegisteredVisitRepository.save(visit);

        realtimeQueueGateway.publishPreRegisteredUpdate(savedVisit);

        return Optional.of(savedVisit);
    }

    @Override
    public void handle(MarkNotificationAsReadCommand command) {
        notificationRepository.findById(command.notificationId()).ifPresent(notif -> {
            notif.markRead();
            notificationRepository.save(notif);
        });
    }
}
