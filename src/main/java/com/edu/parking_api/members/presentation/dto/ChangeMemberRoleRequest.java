package com.edu.parking_api.members.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeMemberRoleRequest {

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
