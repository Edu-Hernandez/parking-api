package com.edu.parking_api.members.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AddMemberRequest {

    @NotNull(message = "El usuario es obligatorio")
    private UUID userId;

    @NotBlank(message = "El rol es obligatorio")
    @Size(max = 50, message = "El rol no puede tener más de 50 caracteres")
    private String role;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
