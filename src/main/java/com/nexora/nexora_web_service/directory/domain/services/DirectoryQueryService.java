package com.nexora.nexora_web_service.directory.domain.services;

import com.nexora.nexora_web_service.directory.domain.model.entities.Apartment;
import com.nexora.nexora_web_service.directory.domain.model.entities.BuildingDirectory;
import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import com.nexora.nexora_web_service.directory.domain.model.queries.*;

import java.util.Optional;

public interface DirectoryQueryService {
    Optional<BuildingDirectory> handle(GetBuildingByIdQuery query);
    Optional<Apartment> handle(GetApartmentByIdQuery query);
    Optional<ResidentDirectoryProfile> handle(GetResidentByApartmentQuery query);
}
