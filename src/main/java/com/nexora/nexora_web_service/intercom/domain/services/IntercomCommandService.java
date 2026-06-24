package com.nexora.nexora_web_service.intercom.domain.services;

import com.nexora.nexora_web_service.intercom.domain.model.commands.*;
import com.nexora.nexora_web_service.intercom.domain.model.entities.PreRegisteredVisit;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;

import java.util.Optional;

public interface IntercomCommandService {
    Optional<VisitRequest> handle(CreateVisitRequestCommand command);
    Optional<VisitRequest> handle(AttachVisitorEvidenceCommand command);
    Optional<VisitRequest> handle(RegisterAccessDecisionCommand command);
    Optional<PreRegisteredVisit> handle(CreatePreRegisteredVisitCommand command);
    Optional<PreRegisteredVisit> handle(UpdatePreRegisteredVisitCommand command);
    void handle(CancelPreRegisteredVisitCommand command);
    void handle(MarkNotificationAsReadCommand command);
    Optional<PreRegisteredVisit> handle(RegisterPreRegisteredDecisionCommand command);
}
