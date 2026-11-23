package com.edu.parking_api.members.infrastructure.persistence;

import com.edu.parking_api.members.domain.repository.PermissionRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final PermissionJpaRepository permissionJpaRepository;

    public PermissionRepositoryAdapter(PermissionJpaRepository permissionJpaRepository) {
        this.permissionJpaRepository = permissionJpaRepository;
    }

    @Override
    public boolean hasPermission(UUID userId, UUID companyId, String permissionCode) {
        return permissionJpaRepository.hasPermission(userId, companyId, permissionCode);
    }

    @Override
    public Map<UUID, List<String>> findCodesByRole() {
        Map<UUID, List<String>> codesByRole = new HashMap<>();
        for (PermissionJpaRepository.RolePermissionCode row : permissionJpaRepository.findAllRolePermissionCodes()) {
            codesByRole.computeIfAbsent(row.getRoleId(), roleId -> new ArrayList<>()).add(row.getCode());
        }
        return codesByRole;
    }
}
