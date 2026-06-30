package com.nexora.nexora_web_service.onboarding.application.internal.commandservices;

import com.nexora.nexora_web_service.directory.domain.model.commands.AssignResidentCommand;
import com.nexora.nexora_web_service.directory.domain.model.commands.CreateApartmentCommand;
import com.nexora.nexora_web_service.directory.domain.model.commands.CreateBuildingCommand;
import com.nexora.nexora_web_service.directory.domain.model.entities.DoormanBuilding;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ApartmentCode;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ContactChannel;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ResidentDocument;
import com.nexora.nexora_web_service.directory.domain.services.DirectoryCommandService;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.DoormanBuildingRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.iam.domain.model.commands.ConfirmPasswordResetCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RegisterUserCommand;
import com.nexora.nexora_web_service.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountCommandService;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountQueryService;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ClaimDoormanCredentialsCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ClaimResidentCredentialsCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.commands.ProvisionContractCommand;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.ContractProvisionResult;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.GeneratedCredential;
import com.nexora.nexora_web_service.onboarding.domain.model.valueobjects.ResidentClaimOutcome;
import com.nexora.nexora_web_service.onboarding.domain.services.CredentialFactory;
import com.nexora.nexora_web_service.onboarding.domain.services.CredentialMailService;
import com.nexora.nexora_web_service.onboarding.domain.services.OnboardingCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OnboardingCommandServiceImpl implements OnboardingCommandService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingCommandServiceImpl.class);

    private final UserAccountCommandService userAccounts;
    private final UserAccountQueryService userAccountsQuery;
    private final DirectoryCommandService directory;
    private final DoormanBuildingRepository doormanBuildings;
    private final BuildingDirectoryRepository buildings;
    private final ApartmentRepository apartments;
    private final ResidentDirectoryRepository residents;
    private final CredentialMailService mail;

    public OnboardingCommandServiceImpl(UserAccountCommandService userAccounts,
                                        UserAccountQueryService userAccountsQuery,
                                        DirectoryCommandService directory,
                                        DoormanBuildingRepository doormanBuildings,
                                        BuildingDirectoryRepository buildings,
                                        ApartmentRepository apartments,
                                        ResidentDirectoryRepository residents,
                                        CredentialMailService mail) {
        this.userAccounts = userAccounts;
        this.userAccountsQuery = userAccountsQuery;
        this.directory = directory;
        this.doormanBuildings = doormanBuildings;
        this.buildings = buildings;
        this.apartments = apartments;
        this.residents = residents;
        this.mail = mail;
    }

    @Override
    @Transactional
    public ContractProvisionResult handle(ProvisionContractCommand command) {
        // 1. Create the building FIRST — its id yields the per-building letter
        //    that scopes every credential so two contracts never collide.
        String name = (command.buildingName() == null || command.buildingName().isBlank())
                ? command.address() : command.buildingName();
        String address = (command.address() == null || command.address().isBlank())
                ? command.buildingName() : command.address();

        var building = directory.handle(new CreateBuildingCommand(name, address))
                .orElseThrow(() -> new IllegalStateException("Could not register the building in the directory"));

        // Persist the district + photo captured at contract time on the building.
        if ((command.district() != null && !command.district().isBlank())
                || (command.imageUrl() != null && !command.imageUrl().isBlank())) {
            building.setDistrict(command.district());
            building.setImageUrl(command.imageUrl());
            buildings.save(building);
        }

        Long buildingId = building.getId();
        String letter = CredentialFactory.buildingLetter(buildingId);

        // 2. Residents: one IAM account + apartment + directory profile each.
        //    Apartment codes are floor-based (101..10N on floor 1, 201.. on floor 2,
        //    1001.. on floor 10), computed from the building's floor count.
        List<GeneratedCredential> residents = new ArrayList<>();
        int unitsPerFloor = Math.max(1, (int) Math.ceil(command.apartments() / (double) command.floors()));
        for (int i = 0; i < command.apartments(); i++) {
            int floor = i / unitsPerFloor + 1;
            int unit  = i % unitsPerFloor + 1;
            String code = String.valueOf(floor * 100 + unit);   // 101, 102 … 1001
            int n = i + 1;                                       // sequential, used only for the login email
            String email = CredentialFactory.residentEmail(n, letter);
            String password = CredentialFactory.residentPassword();

            Long userId = registerUser(email, password, RoleName.RESIDENT);
            if (userId == null) continue;

            var apartmentOpt = directory.handle(new CreateApartmentCommand(buildingId, code));
            if (apartmentOpt.isEmpty()) continue;
            Long apartmentId = apartmentOpt.get().getId();

            try {
                // Phone stays EMPTY until the resident sets it in the mobile app on
                // first login — that's also what flips them from "pending" to "active".
                directory.handle(new AssignResidentCommand(
                        apartmentId,
                        userId,
                        "Residente " + code,
                        new ResidentDocument("000000" + code),
                        new ContactChannel(email, "")
                ));
            } catch (Exception e) {
                log.warn("Resident assigned partially for {} (apartment {}): {}", email, code, e.getMessage());
            }
            residents.add(new GeneratedCredential("Depto. " + code, email, password, null));
        }

        // 3. Doormen: IAM account + building link carrying their personal email,
        //    and we email them their credentials right away when we have one.
        List<GeneratedCredential> doormen = new ArrayList<>();
        List<String> personalEmails = command.doormanPersonalEmails();
        for (int j = 1; j <= command.doormen(); j++) {
            String email = CredentialFactory.doormanEmail(j, letter);
            String password = CredentialFactory.doormanPassword();
            String personalEmail = j <= personalEmails.size() ? personalEmails.get(j - 1) : "";

            Long userId = registerUser(email, password, RoleName.DOORMAN);
            if (userId == null) continue;

            DoormanBuilding link = doormanBuildings.findByUserId(userId)
                    .orElseGet(() -> new DoormanBuilding(userId, buildingId));
            link.setBuildingId(buildingId);
            link.setLoginEmail(email);
            link.setPersonalEmail(personalEmail);
            // Name defaults to "Portero" until the doorman edits it; phone stays empty.
            if (link.getFullName() == null || link.getFullName().isBlank()) link.setFullName("Portero");
            doormanBuildings.save(link);

            if (personalEmail != null && !personalEmail.isBlank()) {
                safeSend(personalEmail, email, password, building.getName());
            }
            doormen.add(new GeneratedCredential("Portero " + j, email, password, personalEmail));
        }

        log.info("Provisioned building {} ('{}', letter '{}'): {} residents, {} doormen",
                buildingId, building.getName(), letter, residents.size(), doormen.size());
        return new ContractProvisionResult(buildingId, letter, residents, doormen);
    }

    @Override
    @Transactional
    public boolean handle(ClaimDoormanCredentialsCommand command) {
        var linkOpt = doormanBuildings.findByBuildingIdAndPersonalEmail(command.buildingId(), command.personalEmail());
        if (linkOpt.isEmpty()) {
            log.info("Credential claim: no doorman with email {} in building {}", command.personalEmail(), command.buildingId());
            return false;
        }
        var link = linkOpt.get();
        String loginEmail = link.getLoginEmail();
        if (loginEmail == null || loginEmail.isBlank()) return false;

        // Re-issue a fresh password for the doorman's own account and email it.
        String newPassword = CredentialFactory.doormanPassword();
        String token = userAccounts.handle(new RequestPasswordResetCommand(loginEmail));
        if (token == null || token.isBlank()) return false;
        boolean reset = userAccounts.handle(new ConfirmPasswordResetCommand(token, newPassword));
        if (!reset) return false;

        String buildingName = buildings.findById(command.buildingId())
                .map(b -> b.getName()).orElse("tu edificio");
        safeSend(command.personalEmail(), loginEmail, newPassword, buildingName);
        return true;
    }

    @Override
    @Transactional
    public ResidentClaimOutcome handle(ClaimResidentCredentialsCommand command) {
        // 1. Resolve the building by its exact name (case-insensitive). The mobile
        //    autocomplete makes the typed name land exactly on a registered one.
        var buildingOpt = buildings.findAll().stream()
                .filter(b -> b.getName() != null && b.getName().equalsIgnoreCase(command.buildingName().trim()))
                .findFirst();
        if (buildingOpt.isEmpty()) {
            log.info("Resident claim: no building named '{}'", command.buildingName());
            return ResidentClaimOutcome.NOT_FOUND;
        }
        var building = buildingOpt.get();

        // 2. Resolve the apartment by building + code, and make sure it has a resident.
        var apartmentOpt = apartments.findByBuildingIdAndCode(building.getId(), new ApartmentCode(command.apartmentCode().trim()));
        if (apartmentOpt.isEmpty() || apartmentOpt.get().getResidentId() == null) {
            log.info("Resident claim: no apartment {} (with resident) in building {}", command.apartmentCode(), building.getId());
            return ResidentClaimOutcome.NOT_FOUND;
        }
        var apartment = apartmentOpt.get();

        // 3. The resident profile carries the login email and the pending/active marker.
        var profileOpt = residents.findById(apartment.getResidentId());
        if (profileOpt.isEmpty() || profileOpt.get().getContact() == null) return ResidentClaimOutcome.NOT_FOUND;
        var profile = profileOpt.get();

        // SECURITY GATE: a phone on file means the resident already activated this
        // apartment on first login, so it can no longer be claimed by anyone else.
        String phone = profile.getContact().phone();
        if (phone != null && !phone.isBlank()) {
            log.info("Resident claim rejected: apartment {} in building {} is already active", command.apartmentCode(), building.getId());
            return ResidentClaimOutcome.ALREADY_ACTIVE;
        }

        String loginEmail = profile.getContact().email();
        if (loginEmail == null || loginEmail.isBlank()) return ResidentClaimOutcome.NOT_FOUND;

        // 4. Re-issue a fresh password (the original is hashed and cannot be recovered)
        //    and email it to the resident's personal address.
        String newPassword = CredentialFactory.residentPassword();
        String token = userAccounts.handle(new RequestPasswordResetCommand(loginEmail));
        if (token == null || token.isBlank()) return ResidentClaimOutcome.NOT_FOUND;
        boolean reset = userAccounts.handle(new ConfirmPasswordResetCommand(token, newPassword));
        if (!reset) return ResidentClaimOutcome.NOT_FOUND;

        safeSendResident(command.personalEmail(), loginEmail, newPassword, building.getName(), command.apartmentCode().trim());
        return ResidentClaimOutcome.SENT;
    }

    /** Register a user; returns its id, or null if it already exists (additive, never overwrites). */
    private Long registerUser(String email, String password, RoleName role) {
        try {
            return userAccounts.handle(new RegisterUserCommand(email, password, role))
                    .map(UserAccount::getId)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            log.warn("IAM user already exists, leaving it untouched: {}", email);
            return null;
        }
    }

    private void safeSend(String personalEmail, String loginEmail, String password, String buildingName) {
        try {
            mail.sendDoormanCredentials(personalEmail, loginEmail, password, buildingName);
        } catch (Exception e) {
            log.error("Failed to email credentials to {}: {}", personalEmail, e.getMessage());
        }
    }

    private void safeSendResident(String personalEmail, String loginEmail, String password, String buildingName, String apartmentCode) {
        try {
            mail.sendResidentCredentials(personalEmail, loginEmail, password, buildingName, apartmentCode);
        } catch (Exception e) {
            log.error("Failed to email resident credentials to {}: {}", personalEmail, e.getMessage());
        }
    }
}
