package com.edu.parking_api.companies.infrastructure.persistence;

import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;

final class CompanyPersistenceMapper {

    private CompanyPersistenceMapper() {}

    static CompanyJpaEntity toJpaEntity(Company company) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setId(company.getId());
        entity.setBusinessName(company.getBusinessName());
        entity.setTaxId(company.getTaxId());
        entity.setEmail(company.getEmail());
        entity.setPhone(company.getPhone());
        entity.setStatus(company.getStatus().name());
        entity.setCreatedAt(company.getCreatedAt());
        entity.setUpdatedAt(company.getUpdatedAt());
        return entity;
    }

    static Company toDomain(CompanyJpaEntity entity) {
        return Company.reconstitute(
                entity.getId(),
                entity.getBusinessName(),
                entity.getTaxId(),
                entity.getEmail(),
                entity.getPhone(),
                CompanyStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
