package com.edu.parking_api.members.infrastructure.persistence;

import com.edu.parking_api.members.domain.model.CompanyMember;

final class CompanyMemberPersistenceMapper {

    private CompanyMemberPersistenceMapper() {}

    static CompanyMemberJpaEntity toJpaEntity(CompanyMember member) {
        CompanyMemberJpaEntity entity = new CompanyMemberJpaEntity();
        entity.setId(member.getId());
        entity.setCompanyId(member.getCompanyId());
        entity.setUserId(member.getUserId());
        entity.setRoleId(member.getRoleId());
        entity.setCreatedAt(member.getCreatedAt());
        return entity;
    }

    static CompanyMember toDomain(CompanyMemberJpaEntity entity) {
        return CompanyMember.reconstitute(
                entity.getId(),
                entity.getCompanyId(),
                entity.getUserId(),
                entity.getRoleId(),
                entity.getCreatedAt()
        );
    }
}
