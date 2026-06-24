package com.nexora.nexora_web_service.iam.interfaces.acl;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;

public interface IamContextFacade {
    boolean existsUserById(Long userId);
    boolean existsUserByEmail(String email);
    RoleName getUserRole(Long userId);
    String getFcmToken(Long userId);
}
