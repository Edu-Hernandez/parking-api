package com.edu.parking_api.members.infrastructure.persistence;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.members.domain.model.CompanyMember;
import com.edu.parking_api.members.domain.repository.CompanyMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyMemberRepositoryAdapter implements CompanyMemberRepository {

    private static final Sort OLDEST_FIRST = Sort.by(Sort.Direction.ASC, "createdAt");

    private final CompanyMemberJpaRepository companyMemberJpaRepository;

    public CompanyMemberRepositoryAdapter(CompanyMemberJpaRepository companyMemberJpaRepository) {
        this.companyMemberJpaRepository = companyMemberJpaRepository;
    }

    @Override
    public CompanyMember save(CompanyMember member) {
        CompanyMemberJpaEntity entity = CompanyMemberPersistenceMapper.toJpaEntity(member);
        CompanyMemberJpaEntity saved = companyMemberJpaRepository.save(entity);
        return CompanyMemberPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<CompanyMember> findByCompanyIdAndUserId(UUID companyId, UUID userId) {
        return companyMemberJpaRepository.findByCompanyIdAndUserId(companyId, userId)
                .map(CompanyMemberPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId) {
        return companyMemberJpaRepository.existsByCompanyIdAndUserId(companyId, userId);
    }

    @Override
    public PageResult<CompanyMember> findByCompanyId(UUID companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, OLDEST_FIRST);
        Page<CompanyMemberJpaEntity> result = companyMemberJpaRepository.findByCompanyId(companyId, pageable);

        return new PageResult<>(
                result.getContent().stream().map(CompanyMemberPersistenceMapper::toDomain).toList(),
                page,
                size,
                result.getTotalElements()
        );
    }

    @Override
    public List<CompanyMember> findByUserId(UUID userId) {
        return companyMemberJpaRepository.findByUserId(userId).stream()
                .map(CompanyMemberPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(CompanyMember member) {
        companyMemberJpaRepository.deleteById(member.getId());
    }
}
