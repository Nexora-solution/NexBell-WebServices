package com.nexora.nexora_web_service.intercom.infrastructure.gateway;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch;
import com.nexora.nexora_web_service.intercom.domain.services.NotificationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FcmApnsNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(FcmApnsNotificationGateway.class);

    @Override
    public boolean send(NotificationDispatch notification, String fcmToken, Long entityId, String visitorName, String entityType) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("No FCM token available — cannot send push notification for entityId={}", entityId);
            return false;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase Admin SDK is not initialised — push notification for entityId={} was not sent.", entityId);
            return false;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle("NexBell")
                            .setBody("Tu visitante llegó, gestiona su acceso")
                            .build())
                    .putData("id", String.valueOf(entityId))
                    .putData("visitorName", visitorName != null ? visitorName : "")
                    .putData("type", entityType != null ? entityType : "VISIT_REQUEST")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification sent for entityId={} type={} (FCM message id: {})", entityId, entityType, response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification for entityId={}: {}", entityId, e.getMessage());
            return false;
        }
    }
}
