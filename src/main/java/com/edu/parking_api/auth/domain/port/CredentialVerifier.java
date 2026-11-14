package com.edu.parking_api.auth.domain.port;

public interface CredentialVerifier {

    boolean matches(String rawPassword, String passwordHash);
}
