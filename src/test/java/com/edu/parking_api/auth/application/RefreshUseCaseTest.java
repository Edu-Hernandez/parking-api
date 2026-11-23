package com.edu.parking_api.auth.application;

import com.edu.parking_api.auth.application.dto.AuthTokens;
import com.edu.parking_api.auth.domain.exception.InvalidRefreshTokenException;
import com.edu.parking_api.auth.domain.model.RefreshToken;
import com.edu.parking_api.auth.domain.port.TokenProvider;
import com.edu.parking_api.auth.domain.repository.RefreshTokenRepository;
import com.edu.parking_api.support.Fixtures;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshUseCaseTest {

    private final UUID userId = UUID.randomUUID();

    private InMemoryRefreshTokenRepository refreshRepository;
    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;
    private RefreshUseCase refreshUseCase;
    private LogoutUseCase logoutUseCase;

    @BeforeEach
    void setUp() {
        refreshRepository = new InMemoryRefreshTokenRepository();
        userRepository = mock(UserRepository.class);
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.issue(any(), any())).thenReturn("access-token");

        refreshTokenService = new RefreshTokenService(refreshRepository, 14);
        refreshUseCase = new RefreshUseCase(refreshTokenService, userRepository, tokenProvider);
        logoutUseCase = new LogoutUseCase(refreshTokenService);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user(true)));
    }

    @Test
    void rotatesRefreshTokenAndIssuesNewAccessToken() {
        String original = refreshTokenService.issue(userId);

        AuthTokens tokens = refreshUseCase.execute(original);

        assertEquals("access-token", tokens.accessToken());
        assertNotEquals(original, tokens.refreshToken());
        assertTrue(refreshTokenService.find(original).orElseThrow().isRevoked());
        assertFalse(refreshTokenService.find(tokens.refreshToken()).orElseThrow().isRevoked());
    }

    @Test
    void reusingRotatedTokenRevokesAllSessionsOfTheUser() {
        String otherSession = refreshTokenService.issue(userId);
        String original = refreshTokenService.issue(userId);
        AuthTokens rotated = refreshUseCase.execute(original);

        // Reusar el token ya rotado debe fallar y cerrar todas las sesiones.
        assertThrows(InvalidRefreshTokenException.class, () -> refreshUseCase.execute(original));

        assertTrue(refreshTokenService.find(otherSession).orElseThrow().isRevoked());
        assertTrue(refreshTokenService.find(rotated.refreshToken()).orElseThrow().isRevoked());
        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshUseCase.execute(rotated.refreshToken()));
    }

    @Test
    void rejectsExpiredRefreshToken() {
        String raw = "expired-token-value";
        refreshRepository.save(RefreshToken.issue(
                userId,
                RefreshTokenService.hash(raw),
                Duration.ofDays(-1),
                Instant.now()));

        assertThrows(InvalidRefreshTokenException.class, () -> refreshUseCase.execute(raw));
    }

    @Test
    void rejectsUnknownRefreshToken() {
        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshUseCase.execute("no-existe"));
    }

    @Test
    void rejectsRefreshForDeactivatedUser() {
        String raw = refreshTokenService.issue(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(false)));

        assertThrows(InvalidRefreshTokenException.class, () -> refreshUseCase.execute(raw));
    }

    @Test
    void logoutRevokesTokenSoItCannotBeRefreshed() {
        String raw = refreshTokenService.issue(userId);

        logoutUseCase.execute(raw);

        assertThrows(InvalidRefreshTokenException.class, () -> refreshUseCase.execute(raw));
    }

    @Test
    void logoutIsIdempotentAndAcceptsUnknownToken() {
        String raw = refreshTokenService.issue(userId);
        logoutUseCase.execute(raw);

        assertDoesNotThrow(() -> logoutUseCase.execute(raw));
        assertDoesNotThrow(() -> logoutUseCase.execute("no-existe"));
    }

    private User user(boolean active) {
        return Fixtures.user(userId, active, false);
    }

    /** Repositorio en memoria para aislar la lógica de negocio de JPA. */
    static class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

        private final Map<UUID, RefreshToken> store = new HashMap<>();

        @Override
        public RefreshToken save(RefreshToken refreshToken) {
            store.put(refreshToken.getId(), refreshToken);
            return refreshToken;
        }

        @Override
        public Optional<RefreshToken> findByTokenHash(String tokenHash) {
            return store.values().stream()
                    .filter(t -> t.getTokenHash().equals(tokenHash))
                    .findFirst();
        }

        @Override
        public List<RefreshToken> findActiveByUserId(UUID userId) {
            return store.values().stream()
                    .filter(t -> t.getUserId().equals(userId) && !t.isRevoked())
                    .toList();
        }
    }
}
