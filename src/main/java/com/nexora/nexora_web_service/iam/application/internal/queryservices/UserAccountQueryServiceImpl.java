package com.nexora.nexora_web_service.iam.application.internal.queryservices;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByIdQuery;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountQueryService;
import com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserAccountQueryServiceImpl implements UserAccountQueryService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountQueryServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Optional<UserAccount> handle(GetUserAccountByIdQuery query) {
        return userAccountRepository.findById(query.id());
    }

    @Override
    public Optional<UserAccount> handle(GetUserAccountByEmailQuery query) {
        return userAccountRepository.findByEmail(query.email());
    }
}
