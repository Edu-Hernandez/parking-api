package com.edu.parking_api.auth.domain.exception;

/*
 * El usuario está autenticado pero no tiene permiso para la operación (HTTP 403).
 * No extiende AuthenticationFailedException: aquel caso es 401.
 */
public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException() {
        super("No tienes permisos para realizar esta acción");
    }
}
