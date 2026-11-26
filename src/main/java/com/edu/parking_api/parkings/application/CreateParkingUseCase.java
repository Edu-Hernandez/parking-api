package com.edu.parking_api.parkings.application;

import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.members.application.CompanyPermissionGuard;
import com.edu.parking_api.parkings.application.dto.CreateParkingCommand;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingPermissions;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateParkingUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final ParkingRepository parkingRepository;

    public CreateParkingUseCase(
            CompanyPermissionGuard permissionGuard,
            CompanyLookup companyLookup,
            ParkingRepository parkingRepository) {
        this.permissionGuard = permissionGuard;
        this.companyLookup = companyLookup;
        this.parkingRepository = parkingRepository;
    }

    public Parking execute(UUID actorId, CreateParkingCommand command) {
        UUID companyId = command.companyId();
        permissionGuard.require(actorId, companyId, ParkingPermissions.CREATE);
        companyLookup.requireActive(companyId);

        Parking parking = Parking.register(
                companyId,
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
