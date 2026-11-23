package com.edu.parking_api.members.presentation.dto;

import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        boolean privileged,
        List<String> permissions
) {}
