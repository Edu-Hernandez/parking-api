package com.edu.parking_api.users.application.dto;

public record CreateUserCommand(
        String documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String email,
        String password,
        String phone
) {}
