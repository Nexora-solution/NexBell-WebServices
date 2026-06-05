package com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitorEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorEvidenceRepository extends JpaRepository<VisitorEvidence, Long> {}
