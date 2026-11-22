package com.edu.parking_api.auth.infrastructure.persistence;

import com.edu.parking_api.auth.domain.model.RefreshToken;

final class RefreshTokenPersistenceMapper {

    private RefreshTokenPersistenceMapper() {}

    static RefreshTokenJpaEntity toJpaEntity(RefreshToken token) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(token.getId());
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevokedAt(token.getRevokedAt());
        entity.setCreatedAt(token.getCreatedAt());
        return entity;
    }

    static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt()
        );
    }
}
