package com.edu.parking_api.parkings.domain.exception;

import com.edu.parking_api.exception.ResourceNotFoundException;

public class ParkingNotFoundException extends ResourceNotFoundException {

    public ParkingNotFoundException() {
        super("El estacionamiento no existe");
    }
}
