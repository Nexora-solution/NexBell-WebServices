package com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.directory.domain.model.entities.Apartment;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ApartmentCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    Optional<Apartment> findByCode(ApartmentCode code);
    Optional<Apartment> findByBuildingIdAndCode(Long buildingId, ApartmentCode code);
    List<Apartment> findByResidentId(Long residentId);
    List<Apartment> findByBuildingId(Long buildingId);
}
