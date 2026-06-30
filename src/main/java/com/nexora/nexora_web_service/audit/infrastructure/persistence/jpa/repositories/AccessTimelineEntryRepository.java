package com.nexora.nexora_web_service.audit.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.audit.domain.model.entities.AccessTimelineEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTimelineEntryRepository extends JpaRepository<AccessTimelineEntry, Long> {}
