package com.edu.parking_api.exception;

/*
 * Excepción para datos de entrada que pasan la validación de formato pero no son
 * aceptables, por ejemplo un rol que no existe (HTTP 400).
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
