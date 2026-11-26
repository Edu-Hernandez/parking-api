package com.edu.parking_api.parkings.application;

import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.members.application.CompanyPermissionGuard;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingPermissions;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ActivateParkingUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final ParkingLookup parkingLookup;
    private final ParkingRepository parkingRepository;

    public ActivateParkingUseCase(
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
        companyLookup.requireActive(companyId);

        Parking parking = parkingLookup.get(parkingId, companyId);
        parking.activate();
        return parkingRepository.save(parking);
    }
}
