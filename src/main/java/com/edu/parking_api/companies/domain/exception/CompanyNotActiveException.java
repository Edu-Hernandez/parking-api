package com.edu.parking_api.companies.domain.exception;

import com.edu.parking_api.exception.BusinessException;

public class CompanyNotActiveException extends BusinessException {

    public CompanyNotActiveException() {
        super("La empresa no está activa");
    }
}
