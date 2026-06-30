package com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.directory.domain.model.entities.BuildingDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingDirectoryRepository extends JpaRepository<BuildingDirectory, Long> {}
