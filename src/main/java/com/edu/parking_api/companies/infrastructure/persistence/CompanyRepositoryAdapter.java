package com.edu.parking_api.companies.infrastructure.persistence;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyRepositoryAdapter implements CompanyRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final CompanyJpaRepository companyJpaRepository;

    public CompanyRepositoryAdapter(CompanyJpaRepository companyJpaRepository) {
        this.companyJpaRepository = companyJpaRepository;
    }

    @Override
    public Company save(Company company) {
        CompanyJpaEntity entity = CompanyPersistenceMapper.toJpaEntity(company);
        CompanyJpaEntity saved = companyJpaRepository.save(entity);
        return CompanyPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return companyJpaRepository.findById(id).map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public List<Company> findAllByIds(Collection<UUID> ids) {
        return companyJpaRepository.findAllById(ids).stream()
                .map(CompanyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<Company> findAll(CompanyStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);
        Page<CompanyJpaEntity> result = status == null
                ? companyJpaRepository.findAll(pageable)
                : companyJpaRepository.findByStatus(status.name(), pageable);

        return new PageResult<>(
                result.getContent().stream().map(CompanyPersistenceMapper::toDomain).toList(),
                page,
                size,
                result.getTotalElements()
        );
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return companyJpaRepository.existsByTaxId(taxId);
    }

    @Override
    public boolean existsByTaxIdAndIdNot(String taxId, UUID id) {
        return companyJpaRepository.existsByTaxIdAndIdNot(taxId, id);
    }
}
