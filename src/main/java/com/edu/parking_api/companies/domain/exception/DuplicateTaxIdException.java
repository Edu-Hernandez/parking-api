package com.edu.parking_api.companies.domain.exception;

import com.edu.parking_api.exception.BusinessException;

public class DuplicateTaxIdException extends BusinessException {

    public DuplicateTaxIdException() {
        super("Ya existe una empresa con ese identificador fiscal");
    }
}
