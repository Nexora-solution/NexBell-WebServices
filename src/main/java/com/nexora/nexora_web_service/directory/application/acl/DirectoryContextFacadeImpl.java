package com.nexora.nexora_web_service.directory.application.acl;

import com.nexora.nexora_web_service.directory.domain.model.queries.GetApartmentByIdQuery;
import com.nexora.nexora_web_service.directory.domain.model.queries.GetResidentByApartmentQuery;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryQueryService;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.directory.interfaces.acl.DirectoryContextFacade;
import com.nexora.nexora_web_service.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DirectoryContextFacadeImpl implements DirectoryContextFacade {

    private final DirectoryQueryService directoryQueryService;
    private final IamContextFacade iamContextFacade;
    private final ResidentDirectoryRepository residentDirectoryRepository;

    public DirectoryContextFacadeImpl(DirectoryQueryService directoryQueryService,
                                       IamContextFacade iamContextFacade,
                                       ResidentDirectoryRepository residentDirectoryRepository) {
        this.directoryQueryService = directoryQueryService;
        this.iamContextFacade = iamContextFacade;
        this.residentDirectoryRepository = residentDirectoryRepository;
    }

    @Override
    public Optional<Long> findResidentIdByApartmentId(Long apartmentId) {
        if (apartmentId == null) return Optional.empty();
        try {
            return directoryQueryService.handle(new GetResidentByApartmentQuery(apartmentId))
                    .map(profile -> profile.getId());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getResidentEmailByApartmentId(Long apartmentId) {
        if (apartmentId == null) return Optional.empty();
        try {
            return directoryQueryService.handle(new GetResidentByApartmentQuery(apartmentId))
                    .map(profile -> profile.getContact().email());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getResidentFcmTokenByApartmentId(Long apartmentId) {
        if (apartmentId == null) return Optional.empty();
        try {
            return directoryQueryService.handle(new GetResidentByApartmentQuery(apartmentId))
                    .map(profile -> iamContextFacade.getFcmToken(profile.getUserId()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getResidentFcmTokenById(Long residentId) {
        if (residentId == null) return Optional.empty();
        return residentDirectoryRepository.findById(residentId)
                .map(profile -> iamContextFacade.getFcmToken(profile.getUserId()));
    }

    @Override
    public boolean existsApartmentById(Long apartmentId) {
        if (apartmentId == null) return false;
        try {
            return directoryQueryService.handle(new GetApartmentByIdQuery(apartmentId)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
