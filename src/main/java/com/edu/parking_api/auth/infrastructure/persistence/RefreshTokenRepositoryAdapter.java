package com.edu.parking_api.auth.infrastructure.persistence;

import com.edu.parking_api.auth.domain.model.RefreshToken;
import com.edu.parking_api.auth.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = RefreshTokenPersistenceMapper.toJpaEntity(refreshToken);
        RefreshTokenJpaEntity saved = refreshTokenJpaRepository.save(entity);
        return RefreshTokenPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash)
                .map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public List<RefreshToken> findActiveByUserId(UUID userId) {
        return refreshTokenJpaRepository.findByUserIdAndRevokedAtIsNull(userId).stream()
                .map(RefreshTokenPersistenceMapper::toDomain)
                .toList();
    }
}
