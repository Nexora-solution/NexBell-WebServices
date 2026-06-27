package com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.security.domain.model.entities.ResidentFace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResidentFaceRepository extends JpaRepository<ResidentFace, Long> {
    Optional<ResidentFace> findByFaceId(Integer faceId);
    Optional<ResidentFace> findByResidentId(Long residentId);
}
