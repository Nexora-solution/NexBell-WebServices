package com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResidentDirectoryRepository extends JpaRepository<ResidentDirectoryProfile, Long> {
    Optional<ResidentDirectoryProfile> findByUserId(Long userId);
}
