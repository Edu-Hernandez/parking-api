package com.edu.parking_api.parkings.application;

import com.edu.parking_api.parkings.domain.exception.ParkingNotFoundException;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Carga un estacionamiento dentro de su empresa, siempre después de autorizar al actor.
 * Si el estacionamiento existe pero es de otra empresa, responde como si no existiera.
 */
@Component
public class ParkingLookup {

    private final ParkingRepository parkingRepository;

    public ParkingLookup(ParkingRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
    }

    public Parking get(UUID parkingId, UUID companyId) {
        return parkingRepository.findByIdAndCompanyId(parkingId, companyId)
                .orElseThrow(ParkingNotFoundException::new);
    }
}
