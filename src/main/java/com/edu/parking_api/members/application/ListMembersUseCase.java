package com.edu.parking_api.members.application;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.common.Paging;
import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.members.application.dto.MemberDetails;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.MemberPermissions;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListMembersUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final CompanyMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RoleResolver roleResolver;

    public ListMembersUseCase(
            CompanyPermissionGuard permissionGuard,
            CompanyLookup companyLookup,
            CompanyMemberRepository memberRepository,
            UserRepository userRepository,
            RoleResolver roleResolver) {
        this.permissionGuard = permissionGuard;
        this.companyLookup = companyLookup;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.roleResolver = roleResolver;
    }

    public PageResult<MemberDetails> execute(UUID actorId, UUID companyId, int page, int size) {
        permissionGuard.require(actorId, companyId, MemberPermissions.VIEW);
        companyLookup.requireExists(companyId);

        PageResult<CompanyMember> members = memberRepository.findByCompanyId(
                companyId, Paging.page(page), Paging.size(size));

        // Una consulta de usuarios y otra de roles para toda la página, sin N+1.
        List<UUID> userIds = members.items().stream().map(CompanyMember::getUserId).toList();
        Map<UUID, User> users = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<UUID, Role> roles = roleResolver.allById();

        return members.map(member -> MemberDetails.of(
                member,
                users.get(member.getUserId()),
                roles.get(member.getRoleId())));
    }
}
