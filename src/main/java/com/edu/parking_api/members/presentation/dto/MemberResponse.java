package com.edu.parking_api.members.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        boolean active,
        String role,
        Instant joinedAt
) {}
