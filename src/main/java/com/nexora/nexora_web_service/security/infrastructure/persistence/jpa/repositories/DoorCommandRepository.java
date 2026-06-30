package com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.security.domain.model.entities.DoorCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoorCommandRepository extends JpaRepository<DoorCommand, Long> {
    Optional<DoorCommand> findFirstByOrderByIdDesc();
}
