package com.edu.parking_api.members.domain.exception;

import com.edu.parking_api.exception.ResourceNotFoundException;

public class MemberNotFoundException extends ResourceNotFoundException {

    public MemberNotFoundException() {
        super("El usuario no es integrante de la empresa");
    }
}
