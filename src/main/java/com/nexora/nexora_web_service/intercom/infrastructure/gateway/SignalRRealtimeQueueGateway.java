package com.nexora.nexora_web_service.intercom.infrastructure.gateway;

import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import com.nexora.nexora_web_service.intercom.domain.services.RealtimeQueueGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SignalRRealtimeQueueGateway implements RealtimeQueueGateway {

    private static final Logger log = LoggerFactory.getLogger(SignalRRealtimeQueueGateway.class);

    @Override
    public void publishQueueUpdate(VisitRequest request) {
        log.info("Publishing real-time queue update for VisitRequest ID '{}' (Status: '{}').", request.getId(), request.getStatus());
    }
}
