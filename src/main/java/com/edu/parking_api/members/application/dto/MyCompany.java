package com.edu.parking_api.members.application.dto;

import com.edu.parking_api.companies.domain.model.CompanyStatus;

import java.util.List;
import java.util.UUID;

/**
 * Empresa a la que pertenece el usuario, con su rol y permisos dentro de ella.
 */
public record MyCompany(
        UUID companyId,
        String businessName,
        CompanyStatus status,
        String role,
        List<String> permissions
) {}
