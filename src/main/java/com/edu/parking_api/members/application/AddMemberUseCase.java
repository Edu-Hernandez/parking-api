package com.edu.parking_api.members.application;

import com.edu.parking_api.exception.BusinessException;
import com.edu.parking_api.members.application.dto.AddMemberCommand;
import com.edu.parking_api.members.application.dto.MemberDetails;
import com.edu.parking_api.members.domain.exception.DuplicateMembershipException;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.MemberPermissions;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import com.edu.parking_api.users.domain.exception.UserNotFoundException;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddMemberUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final RoleResolver roleResolver;
    private final CompanyMemberRepository memberRepository;
    private final UserRepository userRepository;

    public AddMemberUseCase(
            CompanyPermissionGuard permissionGuard,
            CompanyLookup companyLookup,
            RoleResolver roleResolver,
            CompanyMemberRepository memberRepository,
            UserRepository userRepository) {
        this.permissionGuard = permissionGuard;
        this.companyLookup = companyLookup;
        this.roleResolver = roleResolver;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public MemberDetails execute(UUID actorId, AddMemberCommand command) {
        UUID companyId = command.companyId();
        permissionGuard.require(actorId, companyId, MemberPermissions.ADD);
        companyLookup.requireActive(companyId);

        Role role = roleResolver.resolve(command.role());
        permissionGuard.requireCanManageRole(actorId, companyId, role);

        User user = userRepository.findById(command.userId())
                .orElseThrow(UserNotFoundException::new);
        if (!user.isActive()) {
            throw new BusinessException("El usuario no está activo");
        }
        if (memberRepository.existsByCompanyIdAndUserId(companyId, user.getId())) {
            throw new DuplicateMembershipException();
        }

        CompanyMember member = memberRepository.save(CompanyMember.add(companyId, user.getId(), role.id()));
        return MemberDetails.of(member, user, role);
    }
}
