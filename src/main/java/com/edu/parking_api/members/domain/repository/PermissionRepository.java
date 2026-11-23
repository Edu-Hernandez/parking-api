package com.edu.parking_api.members.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PermissionRepository {

    // Si el rol que tiene el usuario en esa empresa incluye el permiso.
    boolean hasPermission(UUID userId, UUID companyId, String permissionCode);

    // Códigos de permiso de cada rol, indexados por id de rol. Roles sin permisos no aparecen.
    Map<UUID, List<String>> findCodesByRole();
}
