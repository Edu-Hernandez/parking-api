package com.edu.parking_api.companies.domain.repository;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    // Los ids que no existen simplemente no aparecen en el resultado.
    List<Company> findAllByIds(Collection<UUID> ids);

    // status null devuelve empresas de cualquier estado. Orden: más recientes primero.
    PageResult<Company> findAll(CompanyStatus status, int page, int size);

    // El tax_id recibido debe venir normalizado con TaxIdNormalizer.
    boolean existsByTaxId(String taxId);

    boolean existsByTaxIdAndIdNot(String taxId, UUID id);
}
