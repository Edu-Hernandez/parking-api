package com.edu.parking_api.members.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class CompanyMember {

    private UUID id;
    private UUID companyId;
    private UUID userId;
    private UUID roleId;
    private Instant createdAt;

    private CompanyMember() {}

    public static CompanyMember add(UUID companyId, UUID userId, UUID roleId) {
        Objects.requireNonNull(companyId, "companyId es obligatorio");
        Objects.requireNonNull(userId, "userId es obligatorio");
        Objects.requireNonNull(roleId, "roleId es obligatorio");

        CompanyMember member = new CompanyMember();
        member.companyId = companyId;
        member.userId = userId;
        member.roleId = roleId;
        return member;
    }

    public static CompanyMember reconstitute(
            UUID id,
            UUID companyId,
            UUID userId,
            UUID roleId,
            Instant createdAt) {
        CompanyMember member = new CompanyMember();
        member.id = id;
        member.companyId = companyId;
        member.userId = userId;
        member.roleId = roleId;
        member.createdAt = createdAt;
        return member;
    }

    public void changeRole(UUID newRoleId) {
        this.roleId = Objects.requireNonNull(newRoleId, "roleId es obligatorio");
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
