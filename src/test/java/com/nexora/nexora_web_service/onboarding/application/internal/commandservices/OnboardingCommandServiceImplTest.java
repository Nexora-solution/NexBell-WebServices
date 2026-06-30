package com.nexora.nexora_web_service.onboarding.application.internal.commandservices;

import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.DoormanBuildingRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountCommandService;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountQueryService;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ClaimResidentCredentialsCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.ResidentClaimOutcome;
import com.nexora.nexora_web_service.onboarding.domain.services.CredentialMailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingCommandServiceImplTest {

    @Mock UserAccountCommandService userAccounts;
    @Mock UserAccountQueryService userAccountsQuery;
    @Mock DirectoryCommandService directory;
    @Mock DoormanBuildingRepository doormanBuildings;
    @Mock BuildingDirectoryRepository buildings;
    @Mock ApartmentRepository apartments;
    @Mock ResidentDirectoryRepository residents;
    @Mock CredentialMailService mail;

    @InjectMocks OnboardingCommandServiceImpl service;

    @Test
    void claimResident_whenBuildingDoesNotExist_returnsNotFound() {
        when(buildings.findAll()).thenReturn(List.of());

        var outcome = service.handle(
                new ClaimResidentCredentialsCommand("Ghost Tower", "101", "me@gmail.com"));

        assertEquals(ResidentClaimOutcome.NOT_FOUND, outcome);
    }
}
