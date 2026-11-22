package com.edu.parking_api.auth.presentation;

import com.edu.parking_api.auth.application.GetCurrentUserUseCase;
import com.edu.parking_api.auth.application.LoginUseCase;
import com.edu.parking_api.auth.application.LogoutUseCase;
import com.edu.parking_api.auth.application.RefreshUseCase;
import com.edu.parking_api.auth.application.dto.AuthTokens;
import com.edu.parking_api.auth.application.dto.LoginCommand;
import com.edu.parking_api.auth.domain.model.AuthenticatedUser;
import com.edu.parking_api.auth.presentation.dto.AuthTokensResponse;
import com.edu.parking_api.auth.presentation.dto.CurrentUserResponse;
import com.edu.parking_api.auth.presentation.dto.LoginRequest;
import com.edu.parking_api.auth.presentation.dto.RefreshTokenRequest;
import com.edu.parking_api.users.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public AuthController(
            LoginUseCase loginUseCase,
            RefreshUseCase refreshUseCase,
            LogoutUseCase logoutUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = loginUseCase.execute(new LoginCommand(
                request.getEmail(),
                request.getPassword()
        ));
        return ResponseEntity.ok(toResponse(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokens tokens = refreshUseCase.execute(request.getRefreshToken());
        return ResponseEntity.ok(toResponse(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.execute(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@CurrentUser AuthenticatedUser authenticatedUser) {
        User user = getCurrentUserUseCase.execute(authenticatedUser.id());
        return ResponseEntity.ok(new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        ));
    }

    private AuthTokensResponse toResponse(AuthTokens tokens) {
        return new AuthTokensResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
