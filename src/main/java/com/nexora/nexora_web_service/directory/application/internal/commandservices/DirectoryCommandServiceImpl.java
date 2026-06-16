package com.nexora.nexora_web_service.directory.application.internal.commandservices;

import com.nexora.nexora_web_service.directory.domain.model.commands.*;
import com.nexora.nexora_web_service.directory.domain.model.entities.Apartment;
import com.nexora.nexora_web_service.directory.domain.model.entities.BuildingDirectory;
import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ApartmentCode;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DirectoryCommandServiceImpl implements DirectoryCommandService {

    private final BuildingDirectoryRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;
    private final ResidentDirectoryRepository residentRepository;
    private final IamContextFacade iamContextFacade;

    public DirectoryCommandServiceImpl(BuildingDirectoryRepository buildingRepository,
                                       ApartmentRepository apartmentRepository,
                                       ResidentDirectoryRepository residentRepository,
                                       IamContextFacade iamContextFacade) {
        this.buildingRepository = buildingRepository;
        this.apartmentRepository = apartmentRepository;
        this.residentRepository = residentRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public Optional<BuildingDirectory> handle(CreateBuildingCommand command) {
        var building = new BuildingDirectory(command.name(), command.address());
        return Optional.of(buildingRepository.save(building));
    }

    @Override
    public Optional<Apartment> handle(CreateApartmentCommand command) {
        if (!buildingRepository.existsById(command.buildingId())) {
            throw new IllegalArgumentException("Building not found");
        }
        var code = new ApartmentCode(command.code());
        if (apartmentRepository.findByBuildingIdAndCode(command.buildingId(), code).isPresent()) {
            throw new IllegalArgumentException("Apartment with code already exists in this building");
        }
        var apartment = new Apartment(command.buildingId(), code);
        return Optional.of(apartmentRepository.save(apartment));
    }

    @Override
    public Optional<Apartment> handle(AssignResidentCommand command) {
        var apartment = apartmentRepository.findById(command.apartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if (!iamContextFacade.existsUserById(command.userId())) {
            throw new IllegalArgumentException("User account does not exist in IAM context");
        }

        var residentProfile = residentRepository.findByUserId(command.userId())
                .orElseGet(() -> {
                    var newProfile = new ResidentDirectoryProfile(
                            command.userId(),
                            command.fullName(),
                            command.document(),
                            command.contact()
                    );
                    return residentRepository.save(newProfile);
                });

        apartment.assignResident(residentProfile.getId());
        return Optional.of(apartmentRepository.save(apartment));
    }

    @Override
    public Optional<ResidentDirectoryProfile> handle(UpdateResidentContactCommand command) {
        var resident = residentRepository.findById(command.residentId())
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));
        resident.updateContact(command.contact());
        return Optional.of(residentRepository.save(resident));
    }
}
