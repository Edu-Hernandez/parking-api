package com.edu.parking_api.parkings.presentation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos editables de un estacionamiento, compartidos por el alta y la edición.
 * La empresa no viaja aquí: siempre sale de la ruta.
 */
public abstract class ParkingDetailsRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede tener más de 150 caracteres")
    private String name;

    private String description;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede tener más de 255 caracteres")
    private String address;

    @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
    @Digits(integer = 2, fraction = 7, message = "La latitud admite hasta 7 decimales")
    private BigDecimal latitude;

    @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
    @Digits(integer = 3, fraction = 7, message = "La longitud admite hasta 7 decimales")
    private BigDecimal longitude;

    @Size(max = 30, message = "El teléfono no puede tener más de 30 caracteres")
    @Pattern(regexp = "^\\s*\\+?[0-9 ()\\-]*\\s*$",
            message = "El teléfono solo admite dígitos, espacios, guiones, paréntesis y un '+' inicial")
    private String phone;

    @Size(max = 50, message = "La zona horaria no puede tener más de 50 caracteres")
    private String timezone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
