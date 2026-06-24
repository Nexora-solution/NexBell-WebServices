package com.nexora.nexora_web_service.intercom.domain.services;

import com.nexora.nexora_web_service.intercom.domain.model.entities.PreRegisteredVisit;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;

public interface RealtimeQueueGateway {
    void publishQueueUpdate(VisitRequest request);
    void publishPreRegisteredUpdate(PreRegisteredVisit visit);
}
