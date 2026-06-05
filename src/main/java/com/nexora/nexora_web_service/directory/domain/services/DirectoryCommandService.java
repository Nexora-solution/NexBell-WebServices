package com.nexora.nexora_web_service.directory.domain.services;

import com.nexora.nexora_web_service.directory.domain.model.commands.*;
import com.nexora.nexora_web_service.directory.domain.model.entities.Apartment;
import com.nexora.nexora_web_service.directory.domain.model.entities.BuildingDirectory;
import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;

import java.util.Optional;

public interface DirectoryCommandService {
    Optional<BuildingDirectory> handle(CreateBuildingCommand command);
    Optional<Apartment> handle(CreateApartmentCommand command);
    Optional<Apartment> handle(AssignResidentCommand command);
    Optional<ResidentDirectoryProfile> handle(UpdateResidentContactCommand command);
}
