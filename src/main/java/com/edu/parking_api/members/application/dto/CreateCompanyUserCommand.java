package com.edu.parking_api.members.application.dto;

import com.edu.parking_api.users.application.dto.CreateUserCommand;

import java.util.UUID;

public record CreateCompanyUserCommand(
        UUID companyId,
        CreateUserCommand user,
        String role
) {}
