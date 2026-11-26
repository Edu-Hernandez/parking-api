package com.edu.parking_api.parkings.application;

import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.common.Paging;
import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.members.application.CompanyPermissionGuard;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingPermissions;
import com.edu.parking_api.parkings.domain.model.ParkingStatus;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListParkingsUseCase {

    private final CompanyPermissionGuard permissionGuard;
    private final CompanyLookup companyLookup;
    private final ParkingRepository parkingRepository;

    public ListParkingsUseCase(
            CompanyPermissionGuard permissionGuard,
            CompanyLookup companyLookup,
            ParkingRepository parkingRepository) {
        this.permissionGuard = permissionGuard;
        this.companyLookup = companyLookup;
        this.parkingRepository = parkingRepository;
    }

    // status null lista todos. Page y size fuera de rango se ajustan en vez de fallar.
    public PageResult<Parking> execute(UUID actorId, UUID companyId, ParkingStatus status, int page, int size) {
        permissionGuard.require(actorId, companyId, ParkingPermissions.VIEW);
        companyLookup.requireExists(companyId);

        return parkingRepository.findByCompanyId(companyId, status, Paging.page(page), Paging.size(size));
    }
}
