package com.edu.parking_api.parkings.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateParkingCommand(
        UUID companyId,
        String name,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String timezone
) {}
