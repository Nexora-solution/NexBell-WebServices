package com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.intercom.domain.model.entities.VisitRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRequestRepository extends JpaRepository<VisitRequest, Long> {}
