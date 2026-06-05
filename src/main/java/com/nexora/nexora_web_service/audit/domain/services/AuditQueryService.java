package com.nexora.nexora_web_service.audit.domain.services;

import com.nexora.nexora_web_service.audit.domain.model.entities.AccessRecord;
import com.nexora.nexora_web_service.audit.domain.model.entities.AccessTimelineEntry;
import com.nexora.nexora_web_service.audit.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface AuditQueryService {
    Optional<AccessRecord> handle(GetAccessRecordByIdQuery query);
    List<AccessRecord> handle(SearchAccessRecordsQuery query);
    List<AccessRecord> handle(GetAccessRecordsByResidentQuery query);
    List<AccessTimelineEntry> handle(GetHardwareLogsQuery query);
}
