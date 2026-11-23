package com.edu.parking_api.companies.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {

    Page<CompanyJpaEntity> findByStatus(String status, Pageable pageable);

    boolean existsByTaxId(String taxId);

    boolean existsByTaxIdAndIdNot(String taxId, UUID id);
}
