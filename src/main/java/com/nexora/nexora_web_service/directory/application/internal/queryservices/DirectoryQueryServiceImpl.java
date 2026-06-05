package com.nexora.nexora_web_service.directory.application.internal.queryservices;

import com.nexora.nexora_web_service.directory.domain.model.entities.Apartment;
import com.nexora.nexora_web_service.directory.domain.model.entities.BuildingDirectory;
import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import com.nexora.nexora_web_service.directory.domain.model.queries.*;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryQueryService;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DirectoryQueryServiceImpl implements DirectoryQueryService {

    private final BuildingDirectoryRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;
    private final ResidentDirectoryRepository residentRepository;

    public DirectoryQueryServiceImpl(BuildingDirectoryRepository buildingRepository,
                                     ApartmentRepository apartmentRepository,
                                     ResidentDirectoryRepository residentRepository) {
        this.buildingRepository = buildingRepository;
        this.apartmentRepository = apartmentRepository;
        this.residentRepository = residentRepository;
    }

    @Override
    public Optional<BuildingDirectory> handle(GetBuildingByIdQuery query) {
        return buildingRepository.findById(query.buildingId());
    }

    @Override
    public Optional<Apartment> handle(GetApartmentByIdQuery query) {
        return apartmentRepository.findById(query.apartmentId());
    }

    @Override
    public Optional<ResidentDirectoryProfile> handle(GetResidentByApartmentQuery query) {
        var apartment = apartmentRepository.findById(query.apartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));
        if (apartment.getResidentId() == null) {
            return Optional.empty();
        }
        return residentRepository.findById(apartment.getResidentId());
    }
}
