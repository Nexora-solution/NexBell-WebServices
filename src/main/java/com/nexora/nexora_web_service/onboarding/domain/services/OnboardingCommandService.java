package com.nexora.nexora_web_service.onboarding.domain.services;

import com.nexora.nexora_web_service.onboarding.domain.model.commands.ClaimDoormanCredentialsCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ProvisionContractCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.ContractProvisionResult;

public interface OnboardingCommandService {
    ContractProvisionResult handle(ProvisionContractCommand command);
    boolean handle(ClaimDoormanCredentialsCommand command);
}
