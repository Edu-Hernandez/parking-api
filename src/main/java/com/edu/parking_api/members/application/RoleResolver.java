package com.edu.parking_api.members.application;

import com.edu.parking_api.members.domain.exception.RoleNotFoundException;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.RoleRepository;
import org.springframework.stereotype.Component;

/**
 * Convierte el nombre de rol recibido por la API en el rol de la base.
 */
@Component
public class RoleResolver {

    private final RoleRepository roleRepository;

    public RoleResolver(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role resolve(String name) {
        return roleRepository.findByName(Role.normalizeName(name))
                .orElseThrow(() -> new RoleNotFoundException(name));
    }
}
