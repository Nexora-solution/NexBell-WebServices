package com.nexora.nexora_web_service.audit.application.internal.commandservices;

import com.nexora.nexora_web_service.audit.domain.model.commands.RegisterAccessRecordCommand;
import com.nexora.nexora_web_service.audit.domain.model.entities.AccessRecord;
import com.nexora.nexora_web_service.audit.domain.model.entities.AccessTimelineEntry;
import com.nexora.nexora_web_service.audit.domain.model.valueobjects.AccessDecision;
import com.nexora.nexora_web_service.audit.domain.model.valueobjects.CorrelationId;
import com.nexora.nexora_web_service.audit.domain.services.AuditCommandService;
import com.nexora.nexora_web_service.audit.infrastructure.persistence.jpa.repositories.AccessRecordRepository;
import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuditCommandServiceImpl implements AuditCommandService {

    private final AccessRecordRepository recordRepository;

    public AuditCommandServiceImpl(AccessRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    public Optional<AccessRecord> handle(RegisterAccessRecordCommand command) {
        var correlationId = new CorrelationId(command.correlationId());

        var record = recordRepository.findByCorrelationId(correlationId)
                .orElseGet(() -> new AccessRecord(correlationId, command.decision()));

        record.setDecision(command.decision());

        record.addTimelineEntry(new AccessTimelineEntry("Access request received at door intercom"));
        record.addTimelineEntry(new AccessTimelineEntry("Resident notified of arrival"));
        record.addTimelineEntry(new AccessTimelineEntry("Resident registered decision: " + command.decision()));

        if (command.decision() == AccessDecision.APPROVED) {
            record.addTimelineEntry(new AccessTimelineEntry("Physical door unlock signal sent successfully"));
        }

        record.seal();

        return Optional.of(recordRepository.save(record));
    }

    @EventListener
    public void onVisitRequestDecision(VisitRequest visitRequest) {
        if ("APPROVED".equalsIgnoreCase(visitRequest.getStatus()) || "REJECTED".equalsIgnoreCase(visitRequest.getStatus())) {
            var decision = AccessDecision.valueOf(visitRequest.getStatus().toUpperCase());
            var command = new RegisterAccessRecordCommand(visitRequest.getId().toString(), decision);
            this.handle(command);
        }
    }
}
