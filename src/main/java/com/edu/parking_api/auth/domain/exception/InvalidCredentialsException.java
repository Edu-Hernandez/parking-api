package com.edu.parking_api.auth.domain.exception;

public class InvalidCredentialsException extends AuthenticationFailedException {

    public InvalidCredentialsException() {
        super("Correo o contraseña incorrectos");
    }
}
