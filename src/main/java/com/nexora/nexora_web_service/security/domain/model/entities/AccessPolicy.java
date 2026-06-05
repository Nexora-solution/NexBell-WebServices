package com.nexora.nexora_web_service.security.domain.model.entities;

import com.nexora.nexora_web_service.iam.domain.model.valueobjects.RoleName;
import com.nexora.nexora_web_service.security.domain.model.valueobjects.PermissionCode;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "access_policies")
public class AccessPolicy extends AuditableModel {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "access_policy_permissions", joinColumns = @JoinColumn(name = "policy_id"))
    private Set<PermissionCode> permissions = new HashSet<>();

    public AccessPolicy() {
        this.permissions = new HashSet<>();
    }

    public AccessPolicy(RoleName role) {
        this.role = role;
        this.permissions = new HashSet<>();
    }

    public RoleName getRole() {
        return role;
    }

    public void setRole(RoleName role) {
        this.role = role;
    }

    public Set<PermissionCode> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionCode> permissions) {
        this.permissions = permissions;
    }

    public void grantPermission(PermissionCode permission) {
        this.permissions.add(permission);
    }

    public void revokePermission(PermissionCode permission) {
        this.permissions.remove(permission);
    }

    public boolean canExecute(PermissionCode permission) {
        return this.permissions.contains(permission);
    }
}
