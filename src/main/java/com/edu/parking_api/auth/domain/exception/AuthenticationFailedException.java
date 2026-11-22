package com.edu.parking_api.auth.domain.exception;

/*
 * Base de los errores de autenticación (credenciales, access token o refresh token
 * inválidos). Todos se responden con 401 desde un único handler.
 */
public abstract class AuthenticationFailedException extends RuntimeException {

    protected AuthenticationFailedException(String message) {
        super(message);
    }
}
