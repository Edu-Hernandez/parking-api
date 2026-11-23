package com.edu.parking_api.auth.application;

import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlatformOwnerGuardTest {

    private final UUID userId = UUID.randomUUID();

    private UserRepository userRepository;
    private PlatformOwnerGuard guard;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        guard = new PlatformOwnerGuard(userRepository);
    }

    @Test
    void allowsActivePlatformOwner() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(true, true)));

        assertDoesNotThrow(() -> guard.requirePlatformOwner(userId));
    }

    @Test
    void rejectsRegularUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(true, false)));

        assertThrows(PermissionDeniedException.class, () -> guard.requirePlatformOwner(userId));
    }

    @Test
    void rejectsInactivePlatformOwner() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(false, true)));

        assertThrows(PermissionDeniedException.class, () -> guard.requirePlatformOwner(userId));
    }

    @Test
    void rejectsUnknownUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(PermissionDeniedException.class, () -> guard.requirePlatformOwner(userId));
    }

    private User user(boolean active, boolean platformOwner) {
        return User.reconstitute(
                userId,
                "DNI",
                "12345678",
                "Ana",
                "Pérez",
                "ana@example.com",
                "hash",
                null,
                active,
                platformOwner,
                Instant.now(),
                Instant.now()
        );
    }
}
