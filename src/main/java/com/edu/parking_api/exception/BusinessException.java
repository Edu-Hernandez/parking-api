package com.edu.parking_api.exception;

/*
 * Excepción personalizada para representar errores de negocio.
 * Esta excepción se lanza cuando ocurren errores lógicos o de negocio.
 * Por ejemplo, cuando un usuario intenta registrarse con un correo electrónico ya existente.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}