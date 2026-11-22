package com.edu.parking_api.auth.domain.model;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email
) {}
