package com.nexora.nexora_web_service.intercom.domain.services;

import com.nexora.nexora_web_service.intercom.domain.model.entities.IntercomQueueItem;
import com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetNotificationHistoryQuery;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetPendingQueueQuery;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetVisitDetailQuery;

import java.util.List;
import java.util.Optional;

public interface IntercomQueryService {
    List<IntercomQueueItem> handle(GetPendingQueueQuery query);
    Optional<VisitRequest> handle(GetVisitDetailQuery query);
    List<NotificationDispatch> handle(GetNotificationHistoryQuery query);
}
