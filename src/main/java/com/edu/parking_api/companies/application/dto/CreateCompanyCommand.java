package com.edu.parking_api.companies.application.dto;

public record CreateCompanyCommand(
        String businessName,
        String taxId,
        String email,
        String phone
) {}
