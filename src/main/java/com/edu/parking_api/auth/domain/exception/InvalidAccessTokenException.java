package com.edu.parking_api.auth.domain.exception;

public class InvalidAccessTokenException extends RuntimeException {

    public InvalidAccessTokenException() {
        super("Token inválido o ausente");
    }
}
