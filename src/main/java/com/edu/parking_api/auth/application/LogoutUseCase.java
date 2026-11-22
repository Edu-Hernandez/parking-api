package com.edu.parking_api.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

    private final RefreshTokenService refreshTokenService;

    public LogoutUseCase(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Revoca el refresh token de la sesión. Es idempotente: un token
     * desconocido o ya revocado no produce error.
     */
    @Transactional
    public void execute(String rawRefreshToken) {
        refreshTokenService.find(rawRefreshToken)
                .filter(token -> !token.isRevoked())
                .ifPresent(refreshTokenService::revoke);
    }
}
