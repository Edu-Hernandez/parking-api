package com.edu.parking_api.companies.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyLookup companyLookup;
    private final PlatformOwnerGuard platformOwnerGuard;

    public DeactivateCompanyUseCase(
            CompanyRepository companyRepository,
            CompanyLookup companyLookup,
            PlatformOwnerGuard platformOwnerGuard) {
        this.companyRepository = companyRepository;
        this.companyLookup = companyLookup;
        this.platformOwnerGuard = platformOwnerGuard;
    }

    public Company execute(UUID actorId, UUID companyId) {
        platformOwnerGuard.requirePlatformOwner(actorId);

        Company company = companyLookup.get(companyId);

        company.deactivate();
        return companyRepository.save(company);
    }
}
