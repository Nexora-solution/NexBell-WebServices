package com.nexora.nexora_web_service.directory.domain.model.queries;

public record GetApartmentByIdQuery(Long apartmentId) {
    public GetApartmentByIdQuery {
        if (apartmentId == null || apartmentId <= 0) throw new IllegalArgumentException("Apartment ID must be positive");
    }
}
