package com.nexora.nexora_web_service.intercom.domain.services;

import com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch;

public interface NotificationGateway {
    /**
     * Sends a push notification to the resident's device about a visit
     * needing a decision.
     *
     * @param notification  the dispatch record being tracked
     * @param fcmToken      the resident's Firebase Cloud Messaging device token
     * @param entityId      the ID the resident needs to act on (so the mobile
     *                      app knows which visit to open when the notification is tapped)
     * @param visitorName   the visitor's name, for context in the notification payload
     * @param entityType    "VISIT_REQUEST" or "PRE_REGISTERED_VISIT" — tells the
     *                      mobile app which set of endpoints to call for this entityId
     */
    boolean send(NotificationDispatch notification, String fcmToken, Long entityId, String visitorName, String entityType);
}
