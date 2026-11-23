package com.edu.parking_api.members.presentation.dto;

import com.edu.parking_api.companies.domain.model.CompanyStatus;

import java.util.List;
import java.util.UUID;

public record MyCompanyResponse(
        UUID companyId,
        String businessName,
        CompanyStatus status,
        String role,
        List<String> permissions
) {}
