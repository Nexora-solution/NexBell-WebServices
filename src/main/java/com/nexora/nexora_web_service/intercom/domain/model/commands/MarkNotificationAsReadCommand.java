package com.nexora.nexora_web_service.intercom.domain.model.commands;

public record MarkNotificationAsReadCommand(Long notificationId) {
    public MarkNotificationAsReadCommand {
        if (notificationId == null || notificationId <= 0) throw new IllegalArgumentException("Notification ID must be positive");
    }
}
