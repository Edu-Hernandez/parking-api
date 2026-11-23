package com.edu.parking_api.members.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.members.domain.model.MemberPermissions;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Autoriza operaciones dentro de una empresa. El owner de la plataforma tiene
 * acceso total; el resto necesita que el rol que tiene en esa empresa incluya
 * el permiso. Qué rol tiene cada permiso se lee de la base en cada llamada.
 */
@Service
public class CompanyPermissionGuard {

    private final PlatformOwnerGuard platformOwnerGuard;
    private final PermissionRepository permissionRepository;

    public CompanyPermissionGuard(PlatformOwnerGuard platformOwnerGuard, PermissionRepository permissionRepository) {
        this.platformOwnerGuard = platformOwnerGuard;
        this.permissionRepository = permissionRepository;
    }

    public boolean has(UUID actorId, UUID companyId, String permissionCode) {
        return platformOwnerGuard.isPlatformOwner(actorId)
                || permissionRepository.hasPermission(actorId, companyId, permissionCode);
    }

    public void require(UUID actorId, UUID companyId, String permissionCode) {
        if (!has(actorId, companyId, permissionCode)) {
            throw new PermissionDeniedException();
        }
    }

    // Tocar a alguien con un rol privilegiado (o asignar uno) exige un permiso adicional.
    public void requireCanManageRole(UUID actorId, UUID companyId, Role role) {
        if (role.privileged()) {
            require(actorId, companyId, MemberPermissions.MANAGE_PRIVILEGED);
        }
    }

    // Regla de negocio fija: nadie modifica su propia membresía, aunque tenga el permiso.
    public void requireNotSelf(UUID actorId, UUID targetUserId) {
        if (actorId.equals(targetUserId)) {
            throw new PermissionDeniedException("No puedes modificar tu propia membresía");
        }
    }
}
