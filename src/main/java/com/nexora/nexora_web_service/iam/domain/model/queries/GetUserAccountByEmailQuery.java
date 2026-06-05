package com.nexora.nexora_web_service.iam.domain.model.queries;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;

public record GetUserAccountByEmailQuery(EmailAddress email) {
    public GetUserAccountByEmailQuery {
        if (email == null) throw new IllegalArgumentException("Email cannot be null");
    }
}
