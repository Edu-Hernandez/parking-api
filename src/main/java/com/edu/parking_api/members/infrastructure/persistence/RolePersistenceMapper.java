package com.edu.parking_api.members.infrastructure.persistence;

import com.edu.parking_api.members.domain.model.Role;

final class RolePersistenceMapper {

    private RolePersistenceMapper() {}

    static Role toDomain(RoleJpaEntity entity) {
        return new Role(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                Boolean.TRUE.equals(entity.getIsPrivileged())
        );
    }
}
