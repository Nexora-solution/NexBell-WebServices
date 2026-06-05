package com.nexora.nexora_web_service.intercom.domain.services;

import com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch;

public interface NotificationGateway {
    boolean send(NotificationDispatch notification, String targetEmail);
}
