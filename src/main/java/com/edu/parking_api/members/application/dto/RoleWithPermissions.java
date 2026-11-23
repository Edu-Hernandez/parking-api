package com.edu.parking_api.members.application.dto;

import com.edu.parking_api.members.domain.model.Role;

import java.util.List;

public record RoleWithPermissions(
        Role role,
        List<String> permissions
) {}
