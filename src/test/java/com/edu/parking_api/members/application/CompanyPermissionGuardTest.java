package com.edu.parking_api.members.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.members.domain.model.MemberPermissions;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyPermissionGuardTest {

    private final UUID actorId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID otherCompanyId = UUID.randomUUID();

    private PlatformOwnerGuard platformOwnerGuard;
    private PermissionRepository permissionRepository;
    private CompanyPermissionGuard guard;

    @BeforeEach
    void setUp() {
        platformOwnerGuard = mock(PlatformOwnerGuard.class);
        permissionRepository = mock(PermissionRepository.class);
        guard = new CompanyPermissionGuard(platformOwnerGuard, permissionRepository);
    }

    @Test
    void platformOwnerPassesWithoutQueryingPermissions() {
        when(platformOwnerGuard.isPlatformOwner(actorId)).thenReturn(true);

        assertDoesNotThrow(() -> guard.require(actorId, companyId, MemberPermissions.ADD));
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void memberWithPermissionPasses() {
        when(permissionRepository.hasPermission(actorId, companyId, MemberPermissions.ADD)).thenReturn(true);

        assertDoesNotThrow(() -> guard.require(actorId, companyId, MemberPermissions.ADD));
    }

    @Test
    void memberWithoutPermissionIsDenied() {
        assertThrows(PermissionDeniedException.class,
                () -> guard.require(actorId, companyId, MemberPermissions.ADD));
    }

    @Test
    void permissionInOneCompanyDoesNotApplyToAnother() {
        when(permissionRepository.hasPermission(actorId, companyId, MemberPermissions.ADD)).thenReturn(true);

        assertDoesNotThrow(() -> guard.require(actorId, companyId, MemberPermissions.ADD));
        assertThrows(PermissionDeniedException.class,
                () -> guard.require(actorId, otherCompanyId, MemberPermissions.ADD));
    }

    @Test
    void nonPrivilegedRoleNeedsNoExtraPermission() {
        Role employee = new Role(UUID.randomUUID(), "EMPLOYEE", null, false);

        assertDoesNotThrow(() -> guard.requireCanManageRole(actorId, companyId, employee));
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void privilegedRoleRequiresManagePrivilegedPermission() {
        Role owner = new Role(UUID.randomUUID(), "OWNER", null, true);

        assertThrows(PermissionDeniedException.class,
                () -> guard.requireCanManageRole(actorId, companyId, owner));

        when(permissionRepository.hasPermission(actorId, companyId, MemberPermissions.MANAGE_PRIVILEGED))
                .thenReturn(true);
        assertDoesNotThrow(() -> guard.requireCanManageRole(actorId, companyId, owner));
    }

    @Test
    void nobodyModifiesOwnMembership() {
        assertThrows(PermissionDeniedException.class, () -> guard.requireNotSelf(actorId, actorId));
        assertDoesNotThrow(() -> guard.requireNotSelf(actorId, UUID.randomUUID()));
    }
}
