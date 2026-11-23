package com.edu.parking_api.members.domain.exception;

import com.edu.parking_api.exception.BusinessException;

public class DuplicateMembershipException extends BusinessException {

    public DuplicateMembershipException() {
        super("El usuario ya es integrante de la empresa");
    }
}
