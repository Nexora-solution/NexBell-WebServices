package com.nexora.nexora_web_service.intercom.interfaces.rest.resources;

import java.time.LocalDateTime;

public record NotificationResource(
    Long id,
    String status,
    LocalDateTime sentAt,
    boolean isRead
) {}
