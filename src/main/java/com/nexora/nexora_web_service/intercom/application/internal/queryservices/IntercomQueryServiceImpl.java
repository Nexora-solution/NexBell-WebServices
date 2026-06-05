package com.nexora.nexora_web_service.intercom.application.internal.queryservices;

import com.nexora.nexora_web_service.intercom.domain.model.entities.IntercomQueueItem;
import com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetNotificationHistoryQuery;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetPendingQueueQuery;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetVisitDetailQuery;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomQueryService;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.IntercomQueueItemRepository;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.NotificationDispatchRepository;
import com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories.VisitRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class IntercomQueryServiceImpl implements IntercomQueryService {

    private final IntercomQueueItemRepository queueRepository;
    private final VisitRequestRepository visitRequestRepository;
    private final NotificationDispatchRepository notificationRepository;

    public IntercomQueryServiceImpl(IntercomQueueItemRepository queueRepository,
                                   VisitRequestRepository visitRequestRepository,
                                   NotificationDispatchRepository notificationRepository) {
        this.queueRepository = queueRepository;
        this.visitRequestRepository = visitRequestRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<IntercomQueueItem> handle(GetPendingQueueQuery query) {
        return queueRepository.findByStatus("ENQUEUED");
    }

    @Override
    public Optional<VisitRequest> handle(GetVisitDetailQuery query) {
        return visitRequestRepository.findById(query.visitRequestId());
    }

    @Override
    public List<NotificationDispatch> handle(GetNotificationHistoryQuery query) {
        return notificationRepository.findByApartmentId(query.apartmentId());
    }
}
