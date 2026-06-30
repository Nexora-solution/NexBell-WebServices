package com.nexora.nexora_web_service.iam.domain.services;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;

public interface TokenIssuer {
    String issueAccessToken(UserAccount account);
    boolean validate(String jwt);
    String getEmailFromToken(String jwt);
    String getRoleFromToken(String jwt);
}
