package com.edu.parking_api.auth.presentation;

import com.edu.parking_api.auth.application.LoginUseCase;
import com.edu.parking_api.auth.application.dto.LoginCommand;
import com.edu.parking_api.auth.presentation.dto.LoginRequest;
import com.edu.parking_api.auth.presentation.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = loginUseCase.execute(new LoginCommand(
                request.getEmail(),
                request.getPassword()
        ));
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
