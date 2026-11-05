package com.edu.parking_api.users.presentation;

import com.edu.parking_api.users.application.CreateUserUseCase;
import com.edu.parking_api.users.application.dto.CreateUserCommand;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.presentation.dto.CreateUserRequest;
import com.edu.parking_api.users.presentation.dto.CreateUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.getDocumentType(),
                request.getDocumentNumber(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
        );
        User user = createUserUseCase.execute(command);

        CreateUserResponse response = new CreateUserResponse(
                user.getId(),
                user.getDocumentType(),
                user.getDocumentNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                user.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
