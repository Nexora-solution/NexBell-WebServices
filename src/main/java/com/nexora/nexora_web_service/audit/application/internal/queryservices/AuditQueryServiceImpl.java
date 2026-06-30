package com.nexora.nexora_web_service.audit.application.internal.queryservices;

import com.nexora.nexora_web_service.audit.domain.model.entities.AccessRecord;
import com.nexora.nexora_web_service.audit.domain.model.entities.AccessTimelineEntry;
import com.nexora.nexora_web_service.audit.domain.model.queries.*;
import com.nexora.nexora_web_service.audit.domain.services.AuditQueryService;
import com.nexora.nexora_web_service.audit.infrastructure.persistence.jpa.repositories.AccessRecordRepository;
import com.nexora.nexora_web_service.audit.infrastructure.persistence.jpa.repositories.AccessTimelineEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditQueryServiceImpl implements AuditQueryService {

    private final AccessRecordRepository recordRepository;
    private final AccessTimelineEntryRepository timelineRepository;

    public AuditQueryServiceImpl(AccessRecordRepository recordRepository,
                                 AccessTimelineEntryRepository timelineRepository) {
        this.recordRepository = recordRepository;
        this.timelineRepository = timelineRepository;
    }

    @Override
    public Optional<AccessRecord> handle(GetAccessRecordByIdQuery query) {
        return recordRepository.findById(query.id());
    }

    @Override
    public List<AccessRecord> handle(SearchAccessRecordsQuery query) {
        return recordRepository.findAll();
    }

    @Override
    public List<AccessRecord> handle(GetAccessRecordsByResidentQuery query) {
        return recordRepository.findAll();
    }

    @Override
    public List<AccessTimelineEntry> handle(GetHardwareLogsQuery query) {
        return timelineRepository.findAll().stream()
                .filter(entry -> {
                    String d = entry.getEventDescription().toLowerCase();
                    return d.contains("door") || d.contains("unlock") || d.contains("lock") ||
                           d.contains("puerta") || d.contains("movimiento");
                })
                .collect(Collectors.toList());
    }
}
