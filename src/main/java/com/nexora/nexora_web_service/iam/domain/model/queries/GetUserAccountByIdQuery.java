package com.nexora.nexora_web_service.iam.domain.model.queries;

public record GetUserAccountByIdQuery(Long id) {
    public GetUserAccountByIdQuery {
        if (id == null || id <= 0) throw new IllegalArgumentException("Id must be a positive number");
    }
}
