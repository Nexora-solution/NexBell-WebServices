package com.nexora.nexora_web_service.directory.interfaces.acl;

import java.util.Optional;

public interface DirectoryContextFacade {
    Optional<Long> findResidentIdByApartmentId(Long apartmentId);
    Optional<String> getResidentEmailByApartmentId(Long apartmentId);
    Optional<String> getResidentFcmTokenByApartmentId(Long apartmentId);
    Optional<String> getResidentFcmTokenById(Long residentId);
    boolean existsApartmentById(Long apartmentId);
}
