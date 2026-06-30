package com.nexora.nexora_web_service.onboarding.application.internal.outboundservices.acl;

import com.nexora.nexora_web_service.directory.domain.model.entities.ResidentDirectoryProfile;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ApartmentRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.BuildingDirectoryRepository;
import com.nexora.nexora_web_service.directory.infrastructure.persistence.jpa.repositories.ResidentDirectoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Anti-corruption layer: the onboarding context reaches the directory context
 * only through this service, instead of injecting directory repositories into
 * its REST controllers. Keeps the bounded-context boundary explicit.
 */
@Service("onboardingExternalDirectoryService")
public class ExternalDirectoryService {

    private final BuildingDirectoryRepository buildings;
    private final ApartmentRepository apartments;
    private final ResidentDirectoryRepository residents;

    public ExternalDirectoryService(BuildingDirectoryRepository buildings,
                                    ApartmentRepository apartments,
                                    ResidentDirectoryRepository residents) {
        this.buildings = buildings;
        this.apartments = apartments;
        this.residents = residents;
    }

    /** Minimal building view for the mobile credential-request autocomplete. */
    public record BuildingSummary(Long id, String name, String district) {}

    /** Apartment view flagging whether it can still be claimed (pending resident). */
    public record ClaimableApartment(String code, boolean claimable) {}

    public List<BuildingSummary> listBuildings() {
        return buildings.findAll().stream()
                .map(b -> new BuildingSummary(
                        b.getId(),
                        b.getName() == null ? "" : b.getName(),
                        b.getDistrict() == null ? "" : b.getDistrict()))
                .toList();
    }

    public List<ClaimableApartment> listApartments(Long buildingId) {
        return apartments.findByBuildingId(buildingId).stream()
                .map(apt -> new ClaimableApartment(
                        apt.getCode() == null ? "" : apt.getCode().code(),
                        isClaimable(apt.getResidentId())))
                .toList();
    }

    /** Claimable = the apartment has a resident profile that has not activated yet (no phone on file). */
    private boolean isClaimable(Long residentProfileId) {
        if (residentProfileId == null) return false;
        return residents.findById(residentProfileId)
                .map(ResidentDirectoryProfile::getContact)
                .map(contact -> contact.phone() == null || contact.phone().isBlank())
                .orElse(false);
    }
}
