package com.edu.parking_api.companies.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.companies.domain.model.Company;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCompanyUseCase {

    private final CompanyLookup companyLookup;
    private final PlatformOwnerGuard platformOwnerGuard;

    public GetCompanyUseCase(CompanyLookup companyLookup, PlatformOwnerGuard platformOwnerGuard) {
        this.companyLookup = companyLookup;
        this.platformOwnerGuard = platformOwnerGuard;
    }

    public Company execute(UUID actorId, UUID companyId) {
        platformOwnerGuard.requirePlatformOwner(actorId);

        return companyLookup.get(companyId);
    }
}
