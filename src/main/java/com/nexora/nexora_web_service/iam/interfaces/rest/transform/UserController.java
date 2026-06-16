package com.nexora.nexora_web_service.iam.interfaces.rest.transform;

import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.DoormanBuildingRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountQueryService;
import com.nexora.nexora_web_service.iam.interfaces.rest.resources.UserProfileResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iam/users")
@Tag(name = "Users", description = "User profile tracking endpoints")
public class UserController {

    private final UserAccountQueryService userAccountQueryService;
    private final ResidentDirectoryRepository residentDirectoryRepository;
    private final ApartmentRepository apartmentRepository;
    private final DoormanBuildingRepository doormanBuildingRepository;

    public UserController(UserAccountQueryService userAccountQueryService,
                          ResidentDirectoryRepository residentDirectoryRepository,
                          ApartmentRepository apartmentRepository,
                          DoormanBuildingRepository doormanBuildingRepository) {
        this.userAccountQueryService = userAccountQueryService;
        this.residentDirectoryRepository = residentDirectoryRepository;
        this.apartmentRepository = apartmentRepository;
        this.doormanBuildingRepository = doormanBuildingRepository;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user profile details")
    public ResponseEntity<?> getMyProfile() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof String email)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            var accountOpt = userAccountQueryService.handle(new GetUserAccountByEmailQuery(new EmailAddress(email)));
            if (accountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var account = accountOpt.get();
            String roleStr = account.getRole() != null ? account.getRole().name().toLowerCase() : "resident";

            String fullName = email.split("@")[0];
            Long apartmentId = null;
            Long buildingId = null;
            Long residentId = null;

            if ("resident".equals(roleStr)) {
                var profileOpt = residentDirectoryRepository.findByUserId(account.getId());
                if (profileOpt.isPresent()) {
                    var profile = profileOpt.get();
                    fullName = profile.getFullName() != null ? profile.getFullName() : fullName;
                    residentId = profile.getId();
                    var apts = apartmentRepository.findByResidentId(profile.getId());
                    if (!apts.isEmpty()) {
                        var apt = apts.get(apts.size() - 1); // most recent assignment
                        apartmentId = apt.getId();
                        buildingId = apt.getBuildingId();
                    }
                }
            } else if ("doorman".equals(roleStr)) {
                var assignmentOpt = doormanBuildingRepository.findByUserId(account.getId());
                if (assignmentOpt.isPresent()) {
                    buildingId = assignmentOpt.get().getBuildingId();
                }
            }

            String avatar = fullName.length() >= 2 ? fullName.substring(0, 2).toUpperCase() : fullName.toUpperCase();

            var resource = new UserProfileResource(
                    account.getId(),
                    account.getEmail().email(),
                    fullName,
                    roleStr,
                    avatar,
                    apartmentId,
                    buildingId,
                    residentId
            );

            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("ERROR in /me: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
