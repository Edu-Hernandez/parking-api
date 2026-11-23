package com.edu.parking_api.auth.application;

import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Autoriza operaciones reservadas al owner de la plataforma. Lee el flag desde
 * la base en cada llamada, así que promover o degradar al owner aplica de inmediato.
 */
@Service
public class PlatformOwnerGuard {

    private final UserRepository userRepository;

    public PlatformOwnerGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void requirePlatformOwner(UUID userId) {
        boolean allowed = userRepository.findById(userId)
                .filter(User::isActive)
                .filter(User::isPlatformOwner)
                .isPresent();
        if (!allowed) {
            throw new PermissionDeniedException();
        }
    }
}
