package com.edu.parking_api.members.application;

import com.edu.parking_api.members.domain.exception.MemberNotFoundException;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.MemberPermissions;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import com.edu.parking_api.members.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RemoveMemberUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final RoleRepository roleRepository;
    private final CompanyMemberRepository memberRepository;

    public RemoveMemberUseCase(
            CompanyPermissionGuard permissionGuard,
            RoleRepository roleRepository,
            CompanyMemberRepository memberRepository) {
        this.permissionGuard = permissionGuard;
        this.roleRepository = roleRepository;
        this.memberRepository = memberRepository;
    }

    public void execute(UUID actorId, UUID companyId, UUID userId) {
        permissionGuard.require(actorId, companyId, MemberPermissions.REMOVE);
        permissionGuard.requireNotSelf(actorId, userId);

        CompanyMember member = memberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(MemberNotFoundException::new);

        Role role = roleRepository.findById(member.getRoleId())
                .orElseThrow(() -> new IllegalStateException("El rol del integrante no existe"));
        permissionGuard.requireCanManageRole(actorId, companyId, role);

        memberRepository.delete(member);
    }
}
