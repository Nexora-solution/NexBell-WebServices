package com.nexora.nexora_web_service.audit.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.audit.domain.model.entities.AccessRecord;
import com.nexora.nexora_web_service.audit.domain.model.valueobjects.CorrelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRecordRepository extends JpaRepository<AccessRecord, Long> {
    Optional<AccessRecord> findByCorrelationId(CorrelationId correlationId);
}
