package com.edu.parking_api.members.application;

import com.edu.parking_api.companies.domain.exception.CompanyNotActiveException;
import com.edu.parking_api.companies.domain.exception.CompanyNotFoundException;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Valida la empresa sobre la que se opera, siempre después de autorizar al actor.
 */
@Component
public class CompanyLookup {

    private final CompanyRepository companyRepository;

    public CompanyLookup(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public void requireExists(UUID companyId) {
        find(companyId);
    }

    public void requireActive(UUID companyId) {
        if (find(companyId).getStatus() != CompanyStatus.ACTIVE) {
            throw new CompanyNotActiveException();
        }
    }

    private Company find(UUID companyId) {
        return companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);
    }
}
