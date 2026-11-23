package com.edu.parking_api.companies.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.companies.application.dto.UpdateCompanyCommand;
import com.edu.parking_api.companies.domain.exception.CompanyNotFoundException;
import com.edu.parking_api.companies.domain.exception.DuplicateTaxIdException;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.normalizer.TaxIdNormalizer;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final PlatformOwnerGuard platformOwnerGuard;

    public UpdateCompanyUseCase(CompanyRepository companyRepository, PlatformOwnerGuard platformOwnerGuard) {
        this.companyRepository = companyRepository;
        this.platformOwnerGuard = platformOwnerGuard;
    }

    public Company execute(UUID actorId, UpdateCompanyCommand command) {
        platformOwnerGuard.requirePlatformOwner(actorId);

        Company company = companyRepository.findById(command.companyId())
                .orElseThrow(CompanyNotFoundException::new);

        String taxId = TaxIdNormalizer.normalize(command.taxId());
        if (taxId != null && companyRepository.existsByTaxIdAndIdNot(taxId, company.getId())) {
            throw new DuplicateTaxIdException();
        }

        company.updateDetails(
                command.businessName(),
                taxId,
                command.email(),
                command.phone()
        );

        return companyRepository.save(company);
    }
}
