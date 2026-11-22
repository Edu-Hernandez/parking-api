package com.edu.parking_api.auth.application;

import com.edu.parking_api.auth.application.dto.AuthTokens;
import com.edu.parking_api.auth.application.dto.LoginCommand;
import com.edu.parking_api.auth.domain.exception.InvalidCredentialsException;
import com.edu.parking_api.auth.domain.port.CredentialVerifier;
import com.edu.parking_api.auth.domain.port.TokenProvider;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.normalizer.EmailNormalizer;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginUseCase {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final CredentialVerifier credentialVerifier;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginUseCase(
            UserRepository userRepository,
            CredentialVerifier credentialVerifier,
            TokenProvider tokenProvider,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.credentialVerifier = credentialVerifier;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthTokens execute(LoginCommand command) {
        String email = EmailNormalizer.normalize(command.email());
        Optional<User> user = userRepository.findByEmail(email);

        boolean passwordMatches = credentialVerifier.matches(
                command.password(),
                user.map(User::getPasswordHash).orElse(DUMMY_PASSWORD_HASH)
        );

        if (user.isEmpty() || !user.get().isActive() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        User authenticated = user.get();
        String accessToken = tokenProvider.issue(authenticated.getId(), authenticated.getEmail());
        String refreshToken = refreshTokenService.issue(authenticated.getId());
        return new AuthTokens(accessToken, refreshToken);
    }
}
