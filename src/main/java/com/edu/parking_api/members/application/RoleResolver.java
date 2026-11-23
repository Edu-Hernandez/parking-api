package com.edu.parking_api.members.application;

import com.edu.parking_api.members.domain.exception.RoleNotFoundException;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve roles del catálogo: por el nombre que llega en la API, por el id que
 * guarda una membresía, o todos a la vez indexados por id.
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

    // Una membresía siempre apunta a un rol existente.
    public Role byId(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalStateException("El rol " + roleId + " no existe"));
    }

    public Map<UUID, Role> allById() {
        return roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::id, Function.identity()));
    }
}
