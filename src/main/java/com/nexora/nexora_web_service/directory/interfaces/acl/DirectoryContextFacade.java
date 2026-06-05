package com.nexora.nexora_web_service.directory.interfaces.acl;

import java.util.Optional;

public interface DirectoryContextFacade {
    Optional<Long> findResidentIdByApartmentId(Long apartmentId);
    Optional<String> getResidentEmailByApartmentId(Long apartmentId);
    boolean existsApartmentById(Long apartmentId);
}
