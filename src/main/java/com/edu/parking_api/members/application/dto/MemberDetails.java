package com.edu.parking_api.members.application.dto;

import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.users.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Integrante de una empresa con los datos del usuario y el nombre de su rol.
 */
public record MemberDetails(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        boolean active,
        String role,
        Instant joinedAt
) {

    public static MemberDetails of(CompanyMember member, User user, Role role) {
        return new MemberDetails(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isActive(),
                role.name(),
                member.getCreatedAt()
        );
    }
}
