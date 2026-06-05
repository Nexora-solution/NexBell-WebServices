package com.nexora.nexora_web_service.iam.domain.services;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByIdQuery;

import java.util.Optional;

public interface UserAccountQueryService {
    Optional<UserAccount> handle(GetUserAccountByIdQuery query);
    Optional<UserAccount> handle(GetUserAccountByEmailQuery query);
}
