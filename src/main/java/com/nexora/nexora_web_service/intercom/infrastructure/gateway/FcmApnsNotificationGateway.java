package com.nexora.nexora_web_service.intercom.infrastructure.gateway;

import com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch;
import com.nexora.nexora_web_service.intercom.domain.services.NotificationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FcmApnsNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(FcmApnsNotificationGateway.class);

    @Override
    public boolean send(NotificationDispatch notification, String targetEmail) {
        log.info("Sending push notification via FCM/APNs to email '{}'...", targetEmail);
        return true;
    }
}
