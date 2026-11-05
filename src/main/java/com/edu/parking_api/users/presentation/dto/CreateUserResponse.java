package com.edu.parking_api.users.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateUserResponse(
        UUID id,
        String documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        Boolean isActive,
        Instant createdAt
) {}
