package com.edu.parking_api.auth.domain.port;

import com.edu.parking_api.auth.domain.AuthenticatedUser;

import java.util.UUID;

public interface TokenProvider {

    String issue(UUID userId, String email);

    AuthenticatedUser parse(String token);
}
