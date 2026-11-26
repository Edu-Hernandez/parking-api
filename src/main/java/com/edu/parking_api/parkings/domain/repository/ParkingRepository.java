package com.edu.parking_api.parkings.domain.repository;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingStatus;

import java.util.Optional;
import java.util.UUID;

public interface ParkingRepository {

    Parking save(Parking parking);

    // Un estacionamiento de otra empresa no se encuentra: el ámbito es parte de la búsqueda.
    Optional<Parking> findByIdAndCompanyId(UUID id, UUID companyId);

    // status null devuelve estacionamientos de cualquier estado. Orden: más recientes primero.
    PageResult<Parking> findByCompanyId(UUID companyId, ParkingStatus status, int page, int size);
}
