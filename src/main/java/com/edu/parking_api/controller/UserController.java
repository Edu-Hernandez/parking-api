package com.edu.parking_api.controller;

import com.edu.parking_api.dto.CreateUserRequest;
import com.edu.parking_api.dto.CreateUserResponse;
import com.edu.parking_api.entity.User;
import com.edu.parking_api.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController	Define un controlador REST.
// @RequestMapping	Define la ruta base.
// @PostMapping	Atiende peticiones HTTP POST.
// @RequestBody	Convierte el JSON recibido en un DTO.
// @Valid	Ejecuta las validaciones del DTO.

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
            ) {
                User user = userService.createUser(request);

                CreateUserResponse response = new CreateUserResponse(
                        user.getId(),
                        user.getDocumentType(),
                        user.getDocumentNumber(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getIsActive(),
                        user.getCreatedAt()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
