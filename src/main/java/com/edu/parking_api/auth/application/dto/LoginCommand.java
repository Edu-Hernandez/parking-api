package com.edu.parking_api.auth.application.dto;

public record LoginCommand(
        String email,
        String password
) {}
