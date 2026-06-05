package com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.security.domain.model.entities.SecurityAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAlarmRepository extends JpaRepository<SecurityAlarm, Long> {
    List<SecurityAlarm> findByStatus(String status);
}
