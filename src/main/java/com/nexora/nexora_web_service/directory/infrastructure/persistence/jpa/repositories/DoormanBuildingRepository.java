package com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.directory.domain.model.entities.DoormanBuilding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoormanBuildingRepository extends JpaRepository<DoormanBuilding, Long> {
    Optional<DoormanBuilding> findByUserId(Long userId);
}
