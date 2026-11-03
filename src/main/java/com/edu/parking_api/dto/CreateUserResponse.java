package com.edu.parking_api.dto;

import java.time.Instant;
import java.util.UUID;

// record: Es una característica de Java que permite crear clases destinadas principalmente a transportar datos.
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
