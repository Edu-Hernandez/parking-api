package com.edu.parking_api.auth.domain.port;

import java.util.UUID;

public interface TokenProvider {

    String issue(UUID userId, String email);
}
