package com.edu.parking_api.auth.domain.exception;

public class InvalidAccessTokenException extends AuthenticationFailedException {

    public static final String MESSAGE = "Token inválido o ausente";

    public InvalidAccessTokenException() {
        super(MESSAGE);
    }
}
