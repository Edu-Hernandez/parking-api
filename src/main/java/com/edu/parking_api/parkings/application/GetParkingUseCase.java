package com.edu.parking_api.parkings.application;

import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.members.application.CompanyPermissionGuard;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingPermissions;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetParkingUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final ParkingLookup parkingLookup;

    public GetParkingUseCase(
            CompanyPermissionGuard permissionGuard,
            CompanyLookup companyLookup,
            ParkingLookup parkingLookup) {
        this.permissionGuard = permissionGuard;
        this.companyLookup = companyLookup;
        this.parkingLookup = parkingLookup;
    }

    public Parking execute(UUID actorId, UUID companyId, UUID parkingId) {
        permissionGuard.require(actorId, companyId, ParkingPermissions.VIEW);
        companyLookup.requireExists(companyId);

        return parkingLookup.get(parkingId, companyId);
    }
}
