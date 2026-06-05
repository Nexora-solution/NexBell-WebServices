package com.nexora.nexora_web_service.directory.domain.model.queries;

public record GetResidentByApartmentQuery(Long apartmentId) {
    public GetResidentByApartmentQuery {
        if (apartmentId == null || apartmentId <= 0) throw new IllegalArgumentException("Apartment ID must be positive");
    }
}
