package com.edu.parking_api.parkings.application;

import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.members.application.CompanyPermissionGuard;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingPermissions;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Desactiva sin borrar: el registro queda, así las reservas conservan su historial.
 * Se permite aunque la empresa esté inactiva, para poder cerrar sus estacionamientos.
 */
@Service
public class DeactivateParkingUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final ParkingLookup parkingLookup;
    private final ParkingRepository parkingRepository;

    public DeactivateParkingUseCase(
            CompanyPermissionGuard permissionGuard,
            CompanyLookup companyLookup,
            ParkingLookup parkingLookup,
            ParkingRepository parkingRepository) {
        this.permissionGuard = permissionGuard;
        this.companyLookup = companyLookup;
        this.parkingLookup = parkingLookup;
        this.parkingRepository = parkingRepository;
    }

    public Parking execute(UUID actorId, UUID companyId, UUID parkingId) {
        permissionGuard.require(actorId, companyId, ParkingPermissions.CHANGE_STATUS);
        companyLookup.requireExists(companyId);

        Parking parking = parkingLookup.get(parkingId, companyId);
        parking.deactivate();
        return parkingRepository.save(parking);
    }
}
