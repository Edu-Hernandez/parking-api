package com.edu.parking_api.parkings.presentation.dto;

import com.edu.parking_api.parkings.domain.model.ParkingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ParkingResponse(
        UUID id,
        UUID companyId,
        String name,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String timezone,
        ParkingStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
