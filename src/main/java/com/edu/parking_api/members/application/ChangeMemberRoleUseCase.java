package com.edu.parking_api.members.application;

import com.edu.parking_api.members.application.dto.ChangeMemberRoleCommand;
import com.edu.parking_api.members.application.dto.MemberDetails;
import com.edu.parking_api.members.domain.exception.MemberNotFoundException;
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
public class ChangeMemberRoleUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final RoleResolver roleResolver;
    private final CompanyMemberRepository memberRepository;
    private final UserRepository userRepository;

    public ChangeMemberRoleUseCase(
            CompanyPermissionGuard permissionGuard,
            RoleResolver roleResolver,
            CompanyMemberRepository memberRepository,
            UserRepository userRepository) {
        this.permissionGuard = permissionGuard;
        this.roleResolver = roleResolver;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public MemberDetails execute(UUID actorId, ChangeMemberRoleCommand command) {
        UUID companyId = command.companyId();
        permissionGuard.require(actorId, companyId, MemberPermissions.CHANGE_ROLE);
        permissionGuard.requireNotSelf(actorId, command.userId());

        CompanyMember member = memberRepository.findByCompanyIdAndUserId(companyId, command.userId())
                .orElseThrow(MemberNotFoundException::new);

        // Hay que poder gestionar tanto el rol actual como el nuevo.
        Role currentRole = roleResolver.byId(member.getRoleId());
        Role newRole = roleResolver.resolve(command.role());
        permissionGuard.requireCanManageRole(actorId, companyId, currentRole);
        permissionGuard.requireCanManageRole(actorId, companyId, newRole);

        member.changeRole(newRole.id());
        CompanyMember saved = memberRepository.save(member);

        User user = userRepository.findById(saved.getUserId())
                .orElseThrow(UserNotFoundException::new);
        return MemberDetails.of(saved, user, newRole);
    }
}
