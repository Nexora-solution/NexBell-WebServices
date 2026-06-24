package com.nexora.nexora_web_service.iam.application.acl;

import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByIdQuery;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountQueryService;
import com.nexora.nexora_web_service.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserAccountQueryService userAccountQueryService;

    public IamContextFacadeImpl(UserAccountQueryService userAccountQueryService) {
        this.userAccountQueryService = userAccountQueryService;
    }

    @Override
    public boolean existsUserById(Long userId) {
        if (userId == null) return false;
        try {
            return userAccountQueryService.handle(new GetUserAccountByIdQuery(userId)).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean existsUserByEmail(String email) {
        if (email == null) return false;
        try {
            return userAccountQueryService.handle(new GetUserAccountByEmailQuery(new EmailAddress(email))).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public RoleName getUserRole(Long userId) {
        if (userId == null) return null;
        try {
            return userAccountQueryService.handle(new GetUserAccountByIdQuery(userId))
                    .map(account -> account.getRole())
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getFcmToken(Long userId) {
        if (userId == null) return null;
        try {
            return userAccountQueryService.handle(new GetUserAccountByIdQuery(userId))
                    .map(account -> account.getFcmToken())
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
