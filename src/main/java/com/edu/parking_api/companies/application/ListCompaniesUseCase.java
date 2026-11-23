package com.edu.parking_api.companies.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.common.Paging;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListCompaniesUseCase {

    private final CompanyRepository companyRepository;
    private final PlatformOwnerGuard platformOwnerGuard;

    public ListCompaniesUseCase(CompanyRepository companyRepository, PlatformOwnerGuard platformOwnerGuard) {
        this.companyRepository = companyRepository;
        this.platformOwnerGuard = platformOwnerGuard;
    }

    // status null lista todas. Page y size fuera de rango se ajustan en vez de fallar.
    public PageResult<Company> execute(UUID actorId, CompanyStatus status, int page, int size) {
        platformOwnerGuard.requirePlatformOwner(actorId);

        return companyRepository.findAll(status, Paging.page(page), Paging.size(size));
    }
}
