package com.edu.parking_api.members.application;

import com.edu.parking_api.members.application.dto.RoleWithPermissions;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.PermissionRepository;
import com.edu.parking_api.members.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Catálogo de roles con los permisos que tiene cada uno. Es información de solo
 * lectura disponible para cualquier usuario autenticado.
 */
@Service
public class ListRolesUseCase {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public ListRolesUseCase(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<RoleWithPermissions> execute() {
        Map<UUID, List<String>> permissionsByRole = permissionRepository.findCodesByRole();

        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::name))
                .map(role -> new RoleWithPermissions(role, permissionsByRole.getOrDefault(role.id(), List.of())))
                .toList();
    }
}
