package com.edu.parking_api.auth.presentation.dto;

public record AuthTokensResponse(
        String token,
        String refreshToken
) {}
