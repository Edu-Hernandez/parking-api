package com.edu.parking_api.parkings.application;

import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.members.application.CompanyPermissionGuard;
import com.edu.parking_api.parkings.application.dto.UpdateParkingCommand;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingPermissions;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateParkingUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final ParkingLookup parkingLookup;
    private final ParkingRepository parkingRepository;

    public UpdateParkingUseCase(
            CompanyPermissionGuard permissionGuard,
            CompanyLookup companyLookup,
            ParkingLookup parkingLookup,
            ParkingRepository parkingRepository) {
        this.permissionGuard = permissionGuard;
        this.companyLookup = companyLookup;
        this.parkingLookup = parkingLookup;
        this.parkingRepository = parkingRepository;
    }

    public Parking execute(UUID actorId, UpdateParkingCommand command) {
        UUID companyId = command.companyId();
        permissionGuard.require(actorId, companyId, ParkingPermissions.UPDATE);
        companyLookup.requireActive(companyId);

        Parking parking = parkingLookup.get(command.parkingId(), companyId);
        parking.updateDetails(
                command.name(),
                command.description(),
                command.address(),
                command.latitude(),
                command.longitude(),
                command.phone(),
                command.timezone()
        );
        return parkingRepository.save(parking);
    }
}
