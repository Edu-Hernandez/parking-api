package com.edu.parking_api.exception;

/*
 * Excepción para recursos inexistentes (HTTP 404).
 * Por ejemplo, cuando se consulta o edita una empresa con un id que no existe.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
