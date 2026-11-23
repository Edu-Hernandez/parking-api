package com.edu.parking_api.companies.application;

import com.edu.parking_api.companies.domain.exception.CompanyNotActiveException;
import com.edu.parking_api.companies.domain.exception.CompanyNotFoundException;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Carga y valida la empresa sobre la que se opera, siempre después de autorizar al actor.
 */
@Component
public class CompanyLookup {

    private final CompanyRepository companyRepository;

    public CompanyLookup(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company get(UUID companyId) {
        return companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);
    }

    public void requireExists(UUID companyId) {
        get(companyId);
    }

    public void requireActive(UUID companyId) {
        if (get(companyId).getStatus() != CompanyStatus.ACTIVE) {
            throw new CompanyNotActiveException();
        }
    }
}
