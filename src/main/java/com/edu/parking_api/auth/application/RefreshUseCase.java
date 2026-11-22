package com.edu.parking_api.auth.application;

import com.edu.parking_api.auth.application.dto.AuthTokens;
import com.edu.parking_api.auth.domain.exception.InvalidRefreshTokenException;
import com.edu.parking_api.auth.domain.model.RefreshToken;
import com.edu.parking_api.auth.domain.port.TokenProvider;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshUseCase.class);

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public RefreshUseCase(
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            TokenProvider tokenProvider) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Rota el refresh token: el usado queda revocado y se emite uno nuevo
     * junto con un access token nuevo.
     * noRollbackFor: si se detecta reuso, la revocación de todas las sesiones
     * debe persistir aunque la respuesta sea 401.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthTokens execute(String rawRefreshToken) {
        RefreshToken stored = refreshTokenService.find(rawRefreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked()) {
            // Un token ya rotado o cerrado se volvió a usar: posible robo.
            // Se cierran todas las sesiones del usuario.
            log.warn("Reuso de refresh token detectado para el usuario {}; se revocan todas sus sesiones",
                    stored.getUserId());
            refreshTokenService.revokeAllActive(stored.getUserId());
            throw new InvalidRefreshTokenException();
        }

        if (stored.isExpiredAt(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(stored.getUserId())
                .filter(User::isActive)
                .orElseThrow(InvalidRefreshTokenException::new);

        refreshTokenService.revoke(stored);
        String newRefreshToken = refreshTokenService.issue(user.getId());
        String accessToken = tokenProvider.issue(user.getId(), user.getEmail());
        return new AuthTokens(accessToken, newRefreshToken);
    }
}
