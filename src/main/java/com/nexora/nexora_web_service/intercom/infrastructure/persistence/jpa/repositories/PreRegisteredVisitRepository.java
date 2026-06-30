package com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.intercom.domain.model.entities.PreRegisteredVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreRegisteredVisitRepository extends JpaRepository<PreRegisteredVisit, Long> {
    List<PreRegisteredVisit> findByResidentIdAndIsActiveTrue(Long residentId);
    List<PreRegisteredVisit> findAllByOrderByCreatedAtDesc();
}
