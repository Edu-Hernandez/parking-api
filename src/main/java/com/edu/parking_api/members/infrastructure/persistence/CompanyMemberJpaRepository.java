package com.edu.parking_api.members.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberJpaRepository extends JpaRepository<CompanyMemberJpaEntity, UUID> {

    Optional<CompanyMemberJpaEntity> findByCompanyIdAndUserId(UUID companyId, UUID userId);

    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);

    Page<CompanyMemberJpaEntity> findByCompanyId(UUID companyId, Pageable pageable);

    List<CompanyMemberJpaEntity> findByUserId(UUID userId);
}
