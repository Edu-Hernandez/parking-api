package com.edu.parking_api.members.application;

import com.edu.parking_api.members.domain.exception.MemberNotFoundException;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.MemberPermissions;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RemoveMemberUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final RoleResolver roleResolver;
    private final CompanyMemberRepository memberRepository;

    public RemoveMemberUseCase(
            CompanyPermissionGuard permissionGuard,
            RoleResolver roleResolver,
            CompanyMemberRepository memberRepository) {
        this.permissionGuard = permissionGuard;
        this.roleResolver = roleResolver;
        this.memberRepository = memberRepository;
    }

    public void execute(UUID actorId, UUID companyId, UUID userId) {
        permissionGuard.require(actorId, companyId, MemberPermissions.REMOVE);
        permissionGuard.requireNotSelf(actorId, userId);

        CompanyMember member = memberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(MemberNotFoundException::new);

        permissionGuard.requireCanManageRole(actorId, companyId, roleResolver.byId(member.getRoleId()));

        memberRepository.delete(member);
    }
}
