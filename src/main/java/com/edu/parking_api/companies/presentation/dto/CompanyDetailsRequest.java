package com.edu.parking_api.companies.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos editables de una empresa, compartidos por el alta y la edición.
 */
public abstract class CompanyDetailsRequest {

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 200, message = "La razón social no puede tener más de 200 caracteres")
    private String businessName;

    @Size(max = 30, message = "El identificador fiscal no puede tener más de 30 caracteres")
    private String taxId;

    @Email(message = "El correo no es válido")
    @Size(max = 255, message = "El correo no puede tener más de 255 caracteres")
    private String email;

    @Size(max = 30, message = "El teléfono no puede tener más de 30 caracteres")
    private String phone;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
