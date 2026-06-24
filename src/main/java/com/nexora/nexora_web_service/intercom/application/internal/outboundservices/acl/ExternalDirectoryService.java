package com.nexora.nexora_web_service.intercom.application.internal.outboundservices.acl;

import com.nexora.nexora_web_service.directory.interfaces.acl.DirectoryContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExternalDirectoryService {

    private final DirectoryContextFacade directoryContextFacade;

    public ExternalDirectoryService(DirectoryContextFacade directoryContextFacade) {
        this.directoryContextFacade = directoryContextFacade;
    }

    public Optional<Long> fetchResidentIdByApartment(Long apartmentId) {
        return directoryContextFacade.findResidentIdByApartmentId(apartmentId);
    }

    public Optional<String> fetchResidentEmailByApartment(Long apartmentId) {
        return directoryContextFacade.getResidentEmailByApartmentId(apartmentId);
    }

    public Optional<String> fetchResidentFcmTokenByApartment(Long apartmentId) {
        return directoryContextFacade.getResidentFcmTokenByApartmentId(apartmentId);
    }

    public Optional<String> fetchResidentFcmTokenById(Long residentId) {
        return directoryContextFacade.getResidentFcmTokenById(residentId);
    }

    public boolean existsApartment(Long apartmentId) {
        return directoryContextFacade.existsApartmentById(apartmentId);
    }
}
