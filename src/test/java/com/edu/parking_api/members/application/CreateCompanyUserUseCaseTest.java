package com.edu.parking_api.members.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.companies.domain.exception.CompanyNotActiveException;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import com.edu.parking_api.members.application.dto.CreateCompanyUserCommand;
import com.edu.parking_api.members.application.dto.MemberDetails;
import com.edu.parking_api.members.domain.exception.RoleNotFoundException;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import com.edu.parking_api.members.domain.repository.RoleRepository;
import com.edu.parking_api.support.Fixtures;
import com.edu.parking_api.users.application.CreateUserUseCase;
import com.edu.parking_api.users.application.dto.CreateUserCommand;
import com.edu.parking_api.users.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateCompanyUserUseCaseTest {

    private final UUID actorId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID newUserId = UUID.randomUUID();
    private final Role admin = new Role(UUID.randomUUID(), "ADMIN", "Administrador", false);
    private final CreateUserCommand userCommand = new CreateUserCommand(
            "DNI", "12345678", "Ana", "Pérez", "ana@cliente.com", "Password123", null);

    private PlatformOwnerGuard platformOwnerGuard;
    private CompanyRepository companyRepository;
    private CreateUserUseCase createUserUseCase;
    private CompanyMemberRepository memberRepository;
    private CreateCompanyUserUseCase useCase;

    @BeforeEach
    void setUp() {
        platformOwnerGuard = mock(PlatformOwnerGuard.class);
        companyRepository = mock(CompanyRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        createUserUseCase = mock(CreateUserUseCase.class);
        memberRepository = mock(CompanyMemberRepository.class);

        useCase = new CreateCompanyUserUseCase(
                platformOwnerGuard,
                new CompanyLookup(companyRepository),
                new RoleResolver(roleRepository),
                createUserUseCase,
                memberRepository);

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(CompanyStatus.ACTIVE)));
        when(createUserUseCase.execute(userCommand)).thenReturn(newUser());
        when(memberRepository.save(any(CompanyMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsTheAccountAndAssignsItToTheCompany() {
        doNothing().when(platformOwnerGuard).requirePlatformOwner(actorId);

        MemberDetails details = useCase.execute(actorId, new CreateCompanyUserCommand(companyId, userCommand, "admin"));

        assertEquals(newUserId, details.userId());
        assertEquals("ADMIN", details.role());
        verify(memberRepository).save(argThat(member ->
                member.getCompanyId().equals(companyId)
                        && member.getUserId().equals(newUserId)
                        && member.getRoleId().equals(admin.id())));
    }

    @Test
    void nonPlatformOwnerCannotCreateAccountsEvenWithCompanyPermissions() {
        doThrow(new PermissionDeniedException()).when(platformOwnerGuard).requirePlatformOwner(actorId);

        assertThrows(PermissionDeniedException.class,
                () -> useCase.execute(actorId, new CreateCompanyUserCommand(companyId, userCommand, "ADMIN")));
        verifyNoInteractions(createUserUseCase, memberRepository);
    }

    @Test
    void failsBeforeCreatingTheUserWhenCompanyIsNotActive() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company(CompanyStatus.INACTIVE)));

        assertThrows(CompanyNotActiveException.class,
                () -> useCase.execute(actorId, new CreateCompanyUserCommand(companyId, userCommand, "ADMIN")));
        verifyNoInteractions(createUserUseCase);
    }

    @Test
    void failsBeforeCreatingTheUserWhenRoleDoesNotExist() {
        assertThrows(RoleNotFoundException.class,
                () -> useCase.execute(actorId, new CreateCompanyUserCommand(companyId, userCommand, "GHOST")));
        verifyNoInteractions(createUserUseCase);
    }

    // La atomicidad la garantiza Spring con @Transactional: aquí se comprueba que siga declarada.
    @Test
    void executeRunsInsideATransaction() throws NoSuchMethodException {
        assertNotNull(CreateCompanyUserUseCase.class
                .getMethod("execute", UUID.class, CreateCompanyUserCommand.class)
                .getAnnotation(Transactional.class));
    }

    private Company company(CompanyStatus status) {
        return Fixtures.company(companyId, "Cocheras SA", status);
    }

    private User newUser() {
        return Fixtures.user(newUserId, "ana@cliente.com", true, false);
    }
}
