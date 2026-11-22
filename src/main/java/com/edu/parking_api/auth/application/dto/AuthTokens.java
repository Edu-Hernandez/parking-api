package com.edu.parking_api.auth.application.dto;

public record AuthTokens(
    String accessToken,
    String refreshToken
) {}
