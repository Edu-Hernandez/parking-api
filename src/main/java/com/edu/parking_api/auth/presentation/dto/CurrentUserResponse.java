package com.edu.parking_api.auth.presentation.dto;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName
) {}
