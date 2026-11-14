package com.edu.parking_api.auth.infrastructure.security;

import com.edu.parking_api.auth.domain.port.CredentialVerifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptCredentialVerifier implements CredentialVerifier {

    private final PasswordEncoder passwordEncoder;

    public BCryptCredentialVerifier(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
