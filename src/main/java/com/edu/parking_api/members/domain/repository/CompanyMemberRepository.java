package com.edu.parking_api.members.domain.repository;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.members.domain.model.CompanyMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberRepository {

    CompanyMember save(CompanyMember member);

    Optional<CompanyMember> findByCompanyIdAndUserId(UUID companyId, UUID userId);

    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);

    // Orden: los integrantes más antiguos primero.
    PageResult<CompanyMember> findByCompanyId(UUID companyId, int page, int size);

    List<CompanyMember> findByUserId(UUID userId);

    void delete(CompanyMember member);
}
