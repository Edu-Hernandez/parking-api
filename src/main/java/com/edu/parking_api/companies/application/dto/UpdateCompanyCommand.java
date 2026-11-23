package com.edu.parking_api.companies.application.dto;

import java.util.UUID;

public record UpdateCompanyCommand(
        UUID companyId,
        String businessName,
        String taxId,
        String email,
        String phone
) {}
