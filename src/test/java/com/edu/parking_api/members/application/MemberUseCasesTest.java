package com.edu.parking_api.members.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.common.Paging;
import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.companies.domain.exception.CompanyNotActiveException;
import com.edu.parking_api.companies.domain.exception.CompanyNotFoundException;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import com.edu.parking_api.exception.BusinessException;
import com.edu.parking_api.members.application.dto.AddMemberCommand;
import com.edu.parking_api.members.application.dto.ChangeMemberRoleCommand;
import com.edu.parking_api.members.application.dto.MemberDetails;
import com.edu.parking_api.members.application.dto.MyCompany;
import com.edu.parking_api.members.application.dto.RoleWithPermissions;
import com.edu.parking_api.members.domain.exception.DuplicateMembershipException;
import com.edu.parking_api.members.domain.exception.MemberNotFoundException;
import com.edu.parking_api.members.domain.exception.RoleNotFoundException;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.MemberPermissions;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import com.edu.parking_api.members.domain.repository.PermissionRepository;
import com.edu.parking_api.members.domain.repository.RoleRepository;
import com.edu.parking_api.support.Fixtures;
import com.edu.parking_api.users.domain.exception.UserNotFoundException;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberUseCasesTest {

    private final UUID actorId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID targetUserId = UUID.randomUUID();

    private final Role admin = new Role(UUID.randomUUID(), "ADMIN", "Administrador", false);
    private final Role employee = new Role(UUID.randomUUID(), "EMPLOYEE", "Empleado", false);
    private final Role owner = new Role(UUID.randomUUID(), "OWNER", "Propietario", true);

    private PlatformOwnerGuard platformOwnerGuard;
    private PermissionRepository permissionRepository;
    private CompanyRepository companyRepository;
    private CompanyMemberRepository memberRepository;
    private UserRepository userRepository;

    private AddMemberUseCase add;
    private ChangeMemberRoleUseCase changeRole;
    private RemoveMemberUseCase remove;
    private ListMembersUseCase list;
    private ListMyCompaniesUseCase listMyCompanies;
    private ListRolesUseCase listRoles;

    @BeforeEach
    void setUp() {
        platformOwnerGuard = mock(PlatformOwnerGuard.class);
        permissionRepository = mock(PermissionRepository.class);
        companyRepository = mock(CompanyRepository.class);
        memberRepository = mock(CompanyMemberRepository.class);
        userRepository = mock(UserRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);

        CompanyPermissionGuard guard = new CompanyPermissionGuard(platformOwnerGuard, permissionRepository);
        CompanyLookup companyLookup = new CompanyLookup(companyRepository);
        RoleResolver roleResolver = new RoleResolver(roleRepository);

        add = new AddMemberUseCase(guard, companyLookup, roleResolver, memberRepository, userRepository);
        changeRole = new ChangeMemberRoleUseCase(guard, roleResolver, memberRepository, userRepository);
        remove = new RemoveMemberUseCase(guard, roleResolver, memberRepository);
        list = new ListMembersUseCase(guard, companyLookup, memberRepository, userRepository, roleResolver);
        listMyCompanies = new ListMyCompaniesUseCase(memberRepository, companyRepository, roleResolver, permissionRepository);
        listRoles = new ListRolesUseCase(roleRepository, permissionRepository);

        for (Role role : List.of(admin, employee, owner)) {
            when(roleRepository.findByName(role.name())).thenReturn(Optional.of(role));
            when(roleRepository.findById(role.id())).thenReturn(Optional.of(role));
        }
        when(roleRepository.findAll()).thenReturn(List.of(admin, employee, owner));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(CompanyStatus.ACTIVE)));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user(targetUserId, true)));
        when(memberRepository.save(any(CompanyMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ---- Agregar ----

    @Test
    void addWithoutPermissionIsDeniedAndSavesNothing() {
        assertThrows(PermissionDeniedException.class,
                () -> add.execute(actorId, new AddMemberCommand(companyId, targetUserId, "EMPLOYEE")));

        verify(memberRepository, never()).save(any());
    }

    @Test
    void addSavesMemberWithResolvedRole() {
        grant(MemberPermissions.ADD);

        MemberDetails details = add.execute(actorId, new AddMemberCommand(companyId, targetUserId, " employee "));

        assertEquals("EMPLOYEE", details.role());
        assertEquals(targetUserId, details.userId());
        verify(memberRepository).save(argThat(member ->
                member.getCompanyId().equals(companyId)
                        && member.getUserId().equals(targetUserId)
                        && member.getRoleId().equals(employee.id())));
    }

    @Test
    void platformOwnerAddsWithoutHavingAnyRolePermission() {
        when(platformOwnerGuard.isPlatformOwner(actorId)).thenReturn(true);

        assertEquals("EMPLOYEE",
                add.execute(actorId, new AddMemberCommand(companyId, targetUserId, "EMPLOYEE")).role());
        verifyNoInteractions(permissionRepository);
    }

    @Test
    void addRejectsPermissionHeldInAnotherCompany() {
        UUID otherCompanyId = UUID.randomUUID();
        when(companyRepository.findById(otherCompanyId)).thenReturn(Optional.of(company(CompanyStatus.ACTIVE)));
        grant(MemberPermissions.ADD); // solo en companyId

        assertThrows(PermissionDeniedException.class,
                () -> add.execute(actorId, new AddMemberCommand(otherCompanyId, targetUserId, "EMPLOYEE")));
    }

    @Test
    void addRejectsUnknownRole() {
        grant(MemberPermissions.ADD);

        assertThrows(RoleNotFoundException.class,
                () -> add.execute(actorId, new AddMemberCommand(companyId, targetUserId, "GHOST")));
    }

    @Test
    void addPrivilegedRoleNeedsManagePrivilegedPermission() {
        grant(MemberPermissions.ADD);
        AddMemberCommand command = new AddMemberCommand(companyId, targetUserId, "OWNER");

        assertThrows(PermissionDeniedException.class, () -> add.execute(actorId, command));

        grant(MemberPermissions.MANAGE_PRIVILEGED);
        assertEquals("OWNER", add.execute(actorId, command).role());
    }

    @Test
    void addRejectsMissingCompanyAndInactiveCompany() {
        grant(MemberPermissions.ADD);
        UUID missing = UUID.randomUUID();
        when(permissionRepository.hasPermission(actorId, missing, MemberPermissions.ADD)).thenReturn(true);
        when(companyRepository.findById(missing)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class,
                () -> add.execute(actorId, new AddMemberCommand(missing, targetUserId, "EMPLOYEE")));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(CompanyStatus.INACTIVE)));
        assertThrows(CompanyNotActiveException.class,
                () -> add.execute(actorId, new AddMemberCommand(companyId, targetUserId, "EMPLOYEE")));
    }

    @Test
    void addRejectsUnknownOrInactiveUser() {
        grant(MemberPermissions.ADD);
        UUID unknown = UUID.randomUUID();
        when(userRepository.findById(unknown)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> add.execute(actorId, new AddMemberCommand(companyId, unknown, "EMPLOYEE")));

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user(targetUserId, false)));
        assertThrows(BusinessException.class,
                () -> add.execute(actorId, new AddMemberCommand(companyId, targetUserId, "EMPLOYEE")));
    }

    @Test
    void addRejectsDuplicateMembership() {
        grant(MemberPermissions.ADD);
        when(memberRepository.existsByCompanyIdAndUserId(companyId, targetUserId)).thenReturn(true);

        assertThrows(DuplicateMembershipException.class,
                () -> add.execute(actorId, new AddMemberCommand(companyId, targetUserId, "EMPLOYEE")));
        verify(memberRepository, never()).save(any());
    }

    // ---- Cambiar rol ----

    @Test
    void changeRoleWithoutPermissionIsDenied() {
        assertThrows(PermissionDeniedException.class,
                () -> changeRole.execute(actorId, new ChangeMemberRoleCommand(companyId, targetUserId, "ADMIN")));
    }

    @Test
    void changeRoleCannotTargetYourself() {
        grant(MemberPermissions.CHANGE_ROLE);

        assertThrows(PermissionDeniedException.class,
                () -> changeRole.execute(actorId, new ChangeMemberRoleCommand(companyId, actorId, "ADMIN")));
    }

    @Test
    void changeRoleFailsWhenUserIsNotAMember() {
        grant(MemberPermissions.CHANGE_ROLE);

        assertThrows(MemberNotFoundException.class,
                () -> changeRole.execute(actorId, new ChangeMemberRoleCommand(companyId, targetUserId, "ADMIN")));
    }

    @Test
    void changeRoleUpdatesTheMember() {
        grant(MemberPermissions.CHANGE_ROLE);
        givenMember(employee);

        MemberDetails details = changeRole.execute(actorId,
                new ChangeMemberRoleCommand(companyId, targetUserId, "ADMIN"));

        assertEquals("ADMIN", details.role());
        verify(memberRepository).save(argThat(member -> member.getRoleId().equals(admin.id())));
    }

    @Test
    void changeRoleCannotTouchOrGrantPrivilegedRolesWithoutPermission() {
        grant(MemberPermissions.CHANGE_ROLE);

        givenMember(owner); // rol actual privilegiado
        assertThrows(PermissionDeniedException.class,
                () -> changeRole.execute(actorId, new ChangeMemberRoleCommand(companyId, targetUserId, "EMPLOYEE")));

        givenMember(employee); // rol nuevo privilegiado
        assertThrows(PermissionDeniedException.class,
                () -> changeRole.execute(actorId, new ChangeMemberRoleCommand(companyId, targetUserId, "OWNER")));

        verify(memberRepository, never()).save(any());
    }

    // ---- Retirar ----

    @Test
    void removeWithoutPermissionIsDenied() {
        assertThrows(PermissionDeniedException.class, () -> remove.execute(actorId, companyId, targetUserId));
    }

    @Test
    void removeCannotTargetYourself() {
        grant(MemberPermissions.REMOVE);

        assertThrows(PermissionDeniedException.class, () -> remove.execute(actorId, companyId, actorId));
    }

    @Test
    void removeFailsWhenUserIsNotAMember() {
        grant(MemberPermissions.REMOVE);

        assertThrows(MemberNotFoundException.class, () -> remove.execute(actorId, companyId, targetUserId));
    }

    @Test
    void removeDeletesTheMembership() {
        grant(MemberPermissions.REMOVE);
        CompanyMember member = givenMember(employee);

        remove.execute(actorId, companyId, targetUserId);

        verify(memberRepository).delete(member);
    }

    @Test
    void removeCannotTouchPrivilegedMemberWithoutPermission() {
        grant(MemberPermissions.REMOVE);
        givenMember(owner);

        assertThrows(PermissionDeniedException.class, () -> remove.execute(actorId, companyId, targetUserId));
        verify(memberRepository, never()).delete(any());
    }

    // ---- Listar ----

    @Test
    void listWithoutPermissionIsDenied() {
        assertThrows(PermissionDeniedException.class, () -> list.execute(actorId, companyId, 0, 20));
        verify(memberRepository, never()).findByCompanyId(any(), anyInt(), anyInt());
    }

    @Test
    void listReturnsMembersWithUserDataAndClampsPaging() {
        grant(MemberPermissions.VIEW);
        CompanyMember member = CompanyMember.reconstitute(
                UUID.randomUUID(), companyId, targetUserId, admin.id(), Instant.now());
        when(memberRepository.findByCompanyId(eq(companyId), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(member), 0, Paging.MAX_PAGE_SIZE, 1));
        when(userRepository.findAllByIds(List.of(targetUserId))).thenReturn(List.of(user(targetUserId, true)));

        PageResult<MemberDetails> result = list.execute(actorId, companyId, -3, 9999);

        verify(memberRepository).findByCompanyId(companyId, 0, Paging.MAX_PAGE_SIZE);
        assertEquals(1, result.items().size());
        assertEquals("ADMIN", result.items().getFirst().role());
        assertEquals("ana@example.com", result.items().getFirst().email());
    }

    // ---- Mis empresas y catálogo de roles ----

    @Test
    void myCompaniesIncludesRoleAndPermissionsPerCompany() {
        UUID otherCompanyId = UUID.randomUUID();
        when(memberRepository.findByUserId(actorId)).thenReturn(List.of(
                CompanyMember.reconstitute(UUID.randomUUID(), companyId, actorId, admin.id(), Instant.now()),
                CompanyMember.reconstitute(UUID.randomUUID(), otherCompanyId, actorId, employee.id(), Instant.now())));
        when(companyRepository.findAllByIds(any())).thenReturn(List.of(
                company(companyId, "Zeta SA"), company(otherCompanyId, "Alfa SA")));
        when(permissionRepository.findCodesByRole())
                .thenReturn(Map.of(admin.id(), List.of(MemberPermissions.VIEW, MemberPermissions.ADD)));

        List<MyCompany> result = listMyCompanies.execute(actorId);

        assertEquals(2, result.size());
        assertEquals("Alfa SA", result.getFirst().businessName());
        assertEquals("EMPLOYEE", result.getFirst().role());
        assertEquals(List.of(), result.getFirst().permissions());
        assertEquals("ADMIN", result.get(1).role());
        assertEquals(List.of(MemberPermissions.VIEW, MemberPermissions.ADD), result.get(1).permissions());
    }

    @Test
    void myCompaniesIsEmptyWithoutMemberships() {
        when(memberRepository.findByUserId(actorId)).thenReturn(List.of());

        assertTrue(listMyCompanies.execute(actorId).isEmpty());
        verifyNoInteractions(companyRepository);
    }

    @Test
    void rolesCatalogListsPermissionsFromTheRepository() {
        when(permissionRepository.findCodesByRole())
                .thenReturn(Map.of(admin.id(), List.of(MemberPermissions.VIEW)));

        List<RoleWithPermissions> result = listRoles.execute();

        assertEquals(List.of("ADMIN", "EMPLOYEE", "OWNER"),
                result.stream().map(item -> item.role().name()).toList());
        assertEquals(List.of(MemberPermissions.VIEW), result.getFirst().permissions());
        assertEquals(List.of(), result.get(1).permissions());
    }

    // ---- Auxiliares ----

    private void grant(String permissionCode) {
        when(permissionRepository.hasPermission(actorId, companyId, permissionCode)).thenReturn(true);
    }

    private CompanyMember givenMember(Role role) {
        CompanyMember member = CompanyMember.reconstitute(
                UUID.randomUUID(), companyId, targetUserId, role.id(), Instant.now());
        when(memberRepository.findByCompanyIdAndUserId(companyId, targetUserId)).thenReturn(Optional.of(member));
        return member;
    }

    private Company company(CompanyStatus status) {
        return Fixtures.company(companyId, "Cocheras SA", status);
    }

    private Company company(UUID id, String name) {
        return Fixtures.company(id, name, CompanyStatus.ACTIVE);
    }

    private User user(UUID id, boolean active) {
        return Fixtures.user(id, active, false);
    }
}
