package com.edu.parking_api.companies.domain.exception;

import com.edu.parking_api.exception.ResourceNotFoundException;

public class CompanyNotFoundException extends ResourceNotFoundException {

    public CompanyNotFoundException() {
        super("La empresa no existe");
    }
}
