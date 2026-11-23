package com.edu.parking_api.members.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.members.application.dto.CreateCompanyUserCommand;
import com.edu.parking_api.members.application.dto.MemberDetails;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import com.edu.parking_api.users.application.CreateUserUseCase;
import com.edu.parking_api.users.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Crea la cuenta de un cliente y la asigna a su empresa. Es una sola transacción:
 * si falla la membresía no queda un usuario huérfano.
 */
@Service
public class CreateCompanyUserUseCase {

    private final PlatformOwnerGuard platformOwnerGuard;
    private final CompanyLookup companyLookup;
    private final RoleResolver roleResolver;
    private final CreateUserUseCase createUserUseCase;
    private final CompanyMemberRepository memberRepository;

    public CreateCompanyUserUseCase(
            PlatformOwnerGuard platformOwnerGuard,
            CompanyLookup companyLookup,
            RoleResolver roleResolver,
            CreateUserUseCase createUserUseCase,
            CompanyMemberRepository memberRepository) {
        this.platformOwnerGuard = platformOwnerGuard;
        this.companyLookup = companyLookup;
        this.roleResolver = roleResolver;
        this.createUserUseCase = createUserUseCase;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberDetails execute(UUID actorId, CreateCompanyUserCommand command) {
        platformOwnerGuard.requirePlatformOwner(actorId);
        companyLookup.requireActive(command.companyId());
        Role role = roleResolver.resolve(command.role());

        User user = createUserUseCase.execute(command.user());
        CompanyMember member = memberRepository.save(
                CompanyMember.add(command.companyId(), user.getId(), role.id()));

        return MemberDetails.of(member, user, role);
    }
}
