package com.nexora.nexora_web_service.directory.domain.model.queries;

public record GetBuildingByIdQuery(Long buildingId) {
    public GetBuildingByIdQuery {
        if (buildingId == null || buildingId <= 0) throw new IllegalArgumentException("Building ID must be positive");
    }
}
