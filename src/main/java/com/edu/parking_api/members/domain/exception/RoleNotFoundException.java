package com.edu.parking_api.members.domain.exception;

import com.edu.parking_api.exception.InvalidInputException;

public class RoleNotFoundException extends InvalidInputException {

    public RoleNotFoundException(String roleName) {
        super("El rol '" + roleName + "' no existe");
    }
}
