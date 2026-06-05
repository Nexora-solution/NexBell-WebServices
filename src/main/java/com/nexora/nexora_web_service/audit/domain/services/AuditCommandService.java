package com.nexora.nexora_web_service.audit.domain.services;

import com.nexora.nexora_web_service.audit.domain.model.commands.RegisterAccessRecordCommand;
import com.nexora.nexora_web_service.audit.domain.model.entities.AccessRecord;

import java.util.Optional;

public interface AuditCommandService {
    Optional<AccessRecord> handle(RegisterAccessRecordCommand command);
}
