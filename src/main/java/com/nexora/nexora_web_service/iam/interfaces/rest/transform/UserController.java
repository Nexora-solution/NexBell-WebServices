package com.nexora.nexora_web_service.iam.interfaces.rest.transform;

import com.nexora.nexora_web_service.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.nexora.nexora_web_service.iam.domain.services.UserAccountQueryService;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
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

    public UserController(UserAccountQueryService userAccountQueryService,
                          ResidentDirectoryRepository residentDirectoryRepository,
                          ApartmentRepository apartmentRepository) {
        this.userAccountQueryService = userAccountQueryService;
        this.residentDirectoryRepository = residentDirectoryRepository;
        this.apartmentRepository = apartmentRepository;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user profile details")
    public ResponseEntity<?> getMyProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof String email)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        var accountOpt = userAccountQueryService.handle(new GetUserAccountByEmailQuery(new EmailAddress(email)));
        if (accountOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var account = accountOpt.get();
        String roleStr = account.getRole().name().toLowerCase();
        
        String fullName = email.split("@")[0];
        Long apartmentId = null;
        
        if ("resident".equals(roleStr)) {
            var profileOpt = residentDirectoryRepository.findByUserId(account.getId());
            if (profileOpt.isPresent()) {
                var profile = profileOpt.get();
                fullName = profile.getFullName();
                var aptOpt = apartmentRepository.findByResidentId(profile.getId());
                if (aptOpt.isPresent()) {
                    apartmentId = aptOpt.get().getId();
                }
            }
        }
        
        String avatar = fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
        
        var resource = new UserProfileResource(
            account.getId(),
            account.getEmail().email(),
            fullName,
            roleStr,
            avatar,
            apartmentId
        );
        
        return ResponseEntity.ok(resource);
    }
}
