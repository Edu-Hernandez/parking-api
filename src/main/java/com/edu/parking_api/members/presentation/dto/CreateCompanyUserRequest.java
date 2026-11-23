package com.edu.parking_api.members.presentation.dto;

import com.edu.parking_api.users.presentation.dto.CreateUserRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de la cuenta nueva (los mismos del registro de usuarios) más el rol que tendrá en la empresa.
 */
public class CreateCompanyUserRequest extends CreateUserRequest {

    @NotBlank(message = "El rol es obligatorio")
    @Size(max = 50, message = "El rol no puede tener más de 50 caracteres")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
