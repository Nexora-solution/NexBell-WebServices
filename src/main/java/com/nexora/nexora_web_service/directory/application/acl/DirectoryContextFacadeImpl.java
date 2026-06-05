package com.nexora.nexora_web_service.directory.application.acl;

import com.nexora.nexora_web_service.directory.domain.model.queries.GetApartmentByIdQuery;
import com.nexora.nexora_web_service.directory.domain.model.queries.GetResidentByApartmentQuery;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryQueryService;
import com.nexora.nexora_web_service.directory.interfaces.acl.DirectoryContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DirectoryContextFacadeImpl implements DirectoryContextFacade {

    private final DirectoryQueryService directoryQueryService;

    public DirectoryContextFacadeImpl(DirectoryQueryService directoryQueryService) {
        this.directoryQueryService = directoryQueryService;
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
    public boolean existsApartmentById(Long apartmentId) {
        if (apartmentId == null) return false;
        try {
            return directoryQueryService.handle(new GetApartmentByIdQuery(apartmentId)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
