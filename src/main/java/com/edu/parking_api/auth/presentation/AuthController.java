package com.edu.parking_api.auth.presentation;

import com.edu.parking_api.auth.application.GetCurrentUserUseCase;
import com.edu.parking_api.auth.application.LoginUseCase;
import com.edu.parking_api.auth.application.dto.LoginCommand;
import com.edu.parking_api.auth.domain.AuthenticatedUser;
import com.edu.parking_api.auth.presentation.dto.CurrentUserResponse;
import com.edu.parking_api.auth.presentation.dto.LoginRequest;
import com.edu.parking_api.auth.presentation.dto.LoginResponse;
import com.edu.parking_api.users.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public AuthController(LoginUseCase loginUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = loginUseCase.execute(new LoginCommand(
                request.getEmail(),
                request.getPassword()
        ));
        return ResponseEntity.ok(new LoginResponse(token));
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
}
