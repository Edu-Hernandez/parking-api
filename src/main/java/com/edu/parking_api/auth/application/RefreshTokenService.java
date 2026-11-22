package com.edu.parking_api.auth.application;

import com.edu.parking_api.auth.domain.model.RefreshToken;
import com.edu.parking_api.auth.domain.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Emite, busca y revoca refresh tokens. El valor en claro solo existe en la
 * respuesta al cliente; en base de datos se guarda su hash SHA-256.
 */
@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final Duration ttl;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${app.jwt.refresh-expiration-days}") long expirationDays) {
        this.repository = repository;
        this.ttl = Duration.ofDays(expirationDays);
    }

    public String issue(UUID userId) {
        String rawToken = generateRawToken();
        repository.save(RefreshToken.issue(userId, hash(rawToken), ttl, Instant.now()));
        return rawToken;
    }

    public Optional<RefreshToken> find(String rawToken) {
        return repository.findByTokenHash(hash(rawToken));
    }

    public void revoke(RefreshToken token) {
        token.revoke(Instant.now());
        repository.save(token);
    }

    public void revokeAllActive(UUID userId) {
        Instant now = Instant.now();
        for (RefreshToken token : repository.findActiveByUserId(userId)) {
            token.revoke(now);
            repository.save(token);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }
}
