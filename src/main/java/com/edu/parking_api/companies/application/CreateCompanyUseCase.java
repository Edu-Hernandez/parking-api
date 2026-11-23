package com.edu.parking_api.companies.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.companies.application.dto.CreateCompanyCommand;
import com.edu.parking_api.companies.domain.exception.DuplicateTaxIdException;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.normalizer.TaxIdNormalizer;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final PlatformOwnerGuard platformOwnerGuard;

    public CreateCompanyUseCase(CompanyRepository companyRepository, PlatformOwnerGuard platformOwnerGuard) {
        this.companyRepository = companyRepository;
        this.platformOwnerGuard = platformOwnerGuard;
    }

    public Company execute(UUID actorId, CreateCompanyCommand command) {
        platformOwnerGuard.requirePlatformOwner(actorId);

        String taxId = TaxIdNormalizer.normalize(command.taxId());
        if (taxId != null && companyRepository.existsByTaxId(taxId)) {
            throw new DuplicateTaxIdException();
        }

        Company company = Company.register(
                command.businessName(),
                taxId,
                command.email(),
                command.phone()
        );

        return companyRepository.save(company);
    }
}
