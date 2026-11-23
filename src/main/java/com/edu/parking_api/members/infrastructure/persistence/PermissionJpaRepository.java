package com.edu.parking_api.members.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, UUID> {

    // Un solo viaje: membresía del usuario en la empresa -> permisos de su rol.
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM company_members m
                JOIN role_permissions rp ON rp.role_id = m.role_id
                JOIN permissions p ON p.id = rp.permission_id
                WHERE m.user_id = :userId
                  AND m.company_id = :companyId
                  AND p.code = :code
            )
            """, nativeQuery = true)
    boolean hasPermission(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("code") String code);

    @Query(value = """
            SELECT rp.role_id AS roleId, p.code AS code
            FROM role_permissions rp
            JOIN permissions p ON p.id = rp.permission_id
            ORDER BY p.code
            """, nativeQuery = true)
    List<RolePermissionCode> findAllRolePermissionCodes();

    interface RolePermissionCode {
        UUID getRoleId();

        String getCode();
    }
}
