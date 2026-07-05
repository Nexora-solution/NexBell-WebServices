package com.nexora.nexora_web_service.intercom.domain.model.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class VisitRequestTest {

    private VisitRequest visitRequest;

    @BeforeEach
    void setUp() {
        visitRequest = new VisitRequest("Carlos Santana", 301L);
    }

    @Test
    @DisplayName("Debería crear una solicitud de visita con estado PENDING")
    void testVisitRequestCreation() {
        assertNotNull(visitRequest, "La solicitud no debe ser nula");
        assertEquals("PENDING", visitRequest.getStatus(), "El estado inicial debe ser PENDING");
        assertEquals("Carlos Santana", visitRequest.getVisitorName(), "El nombre del visitante debe coincidir");
        assertEquals(301L, visitRequest.getApartmentId(), "El ID del apartamento debe coincidir");
    }

    @Test
    @DisplayName("Debería marcar la solicitud como NOTIFIED")
    void testMarkNotified() {
        NotificationDispatch mockNotification = new NotificationDispatch();
        visitRequest.markNotified(mockNotification);

        assertEquals("NOTIFIED", visitRequest.getStatus(), "El estado debe cambiar a NOTIFIED tras la notificación");
        assertNotNull(visitRequest.getNotification(), "La notificación no debe ser nula");
    }

    @Test
    @DisplayName("Debería registrar la decisión de acceso correctamente (APPROVED)")
    void testRegisterDecision() {
        visitRequest.registerDecision("APPROVED");
        assertEquals("APPROVED", visitRequest.getStatus(), "El estado debe cambiar a APPROVED");
    }

    @Test
    @DisplayName("Debería cerrar la solicitud cambiando el estado a CLOSED")
    void testCloseRequest() {
        visitRequest.close();
        assertEquals("CLOSED", visitRequest.getStatus(), "El estado final debe ser CLOSED");
    }
}
