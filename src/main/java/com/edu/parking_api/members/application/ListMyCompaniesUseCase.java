package com.edu.parking_api.members.application;

import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import com.edu.parking_api.members.application.dto.MyCompany;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.model.Role;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import com.edu.parking_api.members.domain.repository.PermissionRepository;
import com.edu.parking_api.members.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Empresas a las que pertenece el usuario autenticado. No requiere permisos:
 * cada usuario solo ve sus propias membresías.
 */
@Service
public class ListMyCompaniesUseCase {

    private final CompanyMemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public ListMyCompaniesUseCase(
            CompanyMemberRepository memberRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.memberRepository = memberRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<MyCompany> execute(UUID actorId) {
        List<CompanyMember> memberships = memberRepository.findByUserId(actorId);
        if (memberships.isEmpty()) {
            return List.of();
        }

        Map<UUID, Company> companies = companyRepository
                .findAllByIds(memberships.stream().map(CompanyMember::getCompanyId).toList())
                .stream()
                .collect(Collectors.toMap(Company::getId, Function.identity()));
        Map<UUID, Role> roles = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::id, Function.identity()));
        Map<UUID, List<String>> permissionsByRole = permissionRepository.findCodesByRole();

        return memberships.stream()
                .map(membership -> {
                    Company company = companies.get(membership.getCompanyId());
                    Role role = roles.get(membership.getRoleId());
                    return new MyCompany(
                            company.getId(),
                            company.getBusinessName(),
                            company.getStatus(),
                            role.name(),
                            permissionsByRole.getOrDefault(role.id(), List.of()));
                })
                .sorted(Comparator.comparing(MyCompany::businessName))
                .toList();
    }
}
