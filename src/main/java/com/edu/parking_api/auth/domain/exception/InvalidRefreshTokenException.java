package com.edu.parking_api.auth.domain.exception;

public class InvalidRefreshTokenException extends AuthenticationFailedException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido o expirado");
    }
}
