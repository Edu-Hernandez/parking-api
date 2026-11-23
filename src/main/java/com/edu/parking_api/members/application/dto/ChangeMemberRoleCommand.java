package com.edu.parking_api.members.application.dto;

import java.util.UUID;

public record ChangeMemberRoleCommand(
        UUID companyId,
        UUID userId,
        String role
) {}
