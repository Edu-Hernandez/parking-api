package com.edu.parking_api.companies.presentation.dto;

import com.edu.parking_api.companies.domain.model.CompanyStatus;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String businessName,
        String taxId,
        String email,
        String phone,
        CompanyStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
