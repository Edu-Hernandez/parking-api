package com.edu.parking_api.users.infrastructure.persistence;

import com.edu.parking_api.users.domain.model.User;

final class UserPersistenceMapper {

    private UserPersistenceMapper() {}

    static UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setDocumentType(user.getDocumentType());
        entity.setDocumentNumber(user.getDocumentNumber());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setPhone(user.getPhone());
        entity.setIsActive(user.isActive());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }

    static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getPhone(),
                Boolean.TRUE.equals(entity.getIsActive()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
