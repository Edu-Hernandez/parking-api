package com.edu.parking_api.users.domain.exception;

import com.edu.parking_api.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException() {
        super("El usuario no existe");
    }
}
