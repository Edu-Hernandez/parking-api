package com.edu.parking_api.parkings.application;

import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.common.Paging;
import com.edu.parking_api.companies.application.CompanyLookup;
import com.edu.parking_api.companies.domain.exception.CompanyNotActiveException;
import com.edu.parking_api.companies.domain.exception.CompanyNotFoundException;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import com.edu.parking_api.members.application.CompanyPermissionGuard;
import com.edu.parking_api.parkings.application.dto.CreateParkingCommand;
import com.edu.parking_api.parkings.application.dto.UpdateParkingCommand;
import com.edu.parking_api.parkings.domain.exception.ParkingNotFoundException;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingPermissions;
import com.edu.parking_api.parkings.domain.model.ParkingStatus;
import com.edu.parking_api.parkings.domain.repository.ParkingRepository;
import com.edu.parking_api.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParkingUseCasesTest {

    private final UUID actorId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID otherCompanyId = UUID.randomUUID();
    private final UUID parkingId = UUID.randomUUID();

    private CompanyPermissionGuard guard;
    private CompanyRepository companyRepository;
    private ParkingRepository parkingRepository;

    private CreateParkingUseCase create;
    private GetParkingUseCase get;
    private ListParkingsUseCase list;
    private UpdateParkingUseCase update;
    private DeactivateParkingUseCase deactivate;
    private ActivateParkingUseCase activate;

    @BeforeEach
    void setUp() {
        guard = mock(CompanyPermissionGuard.class);
        companyRepository = mock(CompanyRepository.class);
        parkingRepository = mock(ParkingRepository.class);
        when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(Fixtures.company(companyId, "Cocheras SA", CompanyStatus.ACTIVE)));

        CompanyLookup companyLookup = new CompanyLookup(companyRepository);
        ParkingLookup parkingLookup = new ParkingLookup(parkingRepository);

        create = new CreateParkingUseCase(guard, companyLookup, parkingRepository);
        get = new GetParkingUseCase(guard, companyLookup, parkingLookup);
        list = new ListParkingsUseCase(guard, companyLookup, parkingRepository);
        update = new UpdateParkingUseCase(guard, companyLookup, parkingLookup, parkingRepository);
        deactivate = new DeactivateParkingUseCase(guard, companyLookup, parkingLookup, parkingRepository);
        activate = new ActivateParkingUseCase(guard, companyLookup, parkingLookup, parkingRepository);
    }

    // ---- Permisos: sin permiso en la empresa no se toca nada ----

    @Test
    void everyUseCaseRejectsActorWithoutPermissionBeforeTouchingRepositories() {
        doThrow(new PermissionDeniedException()).when(guard).require(eq(actorId), eq(companyId), anyString());

        assertThrows(PermissionDeniedException.class, () -> create.execute(actorId, createCommand()));
        assertThrows(PermissionDeniedException.class, () -> get.execute(actorId, companyId, parkingId));
        assertThrows(PermissionDeniedException.class, () -> list.execute(actorId, companyId, null, 0, 20));
        assertThrows(PermissionDeniedException.class, () -> update.execute(actorId, updateCommand()));
        assertThrows(PermissionDeniedException.class, () -> deactivate.execute(actorId, companyId, parkingId));
        assertThrows(PermissionDeniedException.class, () -> activate.execute(actorId, companyId, parkingId));

        verifyNoInteractions(companyRepository, parkingRepository);
    }

    @Test
    void eachOperationChecksItsOwnPermissionInTheRequestedCompany() {
        when(parkingRepository.findByIdAndCompanyId(parkingId, companyId)).thenReturn(Optional.of(existing()));
        when(parkingRepository.findByCompanyId(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        create.execute(actorId, createCommand());
        verify(guard).require(actorId, companyId, ParkingPermissions.CREATE);

        get.execute(actorId, companyId, parkingId);
        list.execute(actorId, companyId, null, 0, 20);
        verify(guard, times(2)).require(actorId, companyId, ParkingPermissions.VIEW);

        update.execute(actorId, updateCommand());
        verify(guard).require(actorId, companyId, ParkingPermissions.UPDATE);

        deactivate.execute(actorId, companyId, parkingId);
        activate.execute(actorId, companyId, parkingId);
        verify(guard, times(2)).require(actorId, companyId, ParkingPermissions.CHANGE_STATUS);
    }

    // ---- Ámbito de empresa ----

    @Test
    void parkingOfAnotherCompanyIsReportedAsNotFound() {
        when(companyRepository.findById(otherCompanyId))
                .thenReturn(Optional.of(Fixtures.company(otherCompanyId, "Otra SA", CompanyStatus.ACTIVE)));
        when(parkingRepository.findByIdAndCompanyId(parkingId, otherCompanyId)).thenReturn(Optional.empty());

        assertThrows(ParkingNotFoundException.class, () -> get.execute(actorId, otherCompanyId, parkingId));
        assertThrows(ParkingNotFoundException.class, () -> update.execute(actorId,
                new UpdateParkingCommand(otherCompanyId, parkingId, "Sede", null, "Dir", null, null, null, null)));
        assertThrows(ParkingNotFoundException.class, () -> deactivate.execute(actorId, otherCompanyId, parkingId));
        assertThrows(ParkingNotFoundException.class, () -> activate.execute(actorId, otherCompanyId, parkingId));

        verify(parkingRepository, never()).save(any());
    }

    @Test
    void unknownCompanyFailsWithNotFound() {
        UUID unknown = UUID.randomUUID();
        when(companyRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class, () -> get.execute(actorId, unknown, parkingId));
        assertThrows(CompanyNotFoundException.class, () -> list.execute(actorId, unknown, null, 0, 20));
        assertThrows(CompanyNotFoundException.class, () -> create.execute(actorId,
                new CreateParkingCommand(unknown, "Sede", null, "Dir", null, null, null, null)));
    }

    // ---- Alta ----

    @Test
    void createRegistersActiveParkingInTheCompanyFromTheRoute() {
        Parking created = create.execute(actorId, createCommand());

        assertEquals(companyId, created.getCompanyId());
        assertEquals("Sede Centro", created.getName());
        assertEquals(ParkingStatus.ACTIVE, created.getStatus());
        verify(parkingRepository).save(created);
    }

    // ---- Empresa inactiva ----

    @Test
    void inactiveCompanyBlocksCreateUpdateAndActivate() {
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(Fixtures.company(companyId, "Cocheras SA", CompanyStatus.INACTIVE)));

        assertThrows(CompanyNotActiveException.class, () -> create.execute(actorId, createCommand()));
        assertThrows(CompanyNotActiveException.class, () -> update.execute(actorId, updateCommand()));
        assertThrows(CompanyNotActiveException.class, () -> activate.execute(actorId, companyId, parkingId));

        verify(parkingRepository, never()).save(any());
    }

    @Test
    void inactiveCompanyStillAllowsReadingAndDeactivating() {
        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(Fixtures.company(companyId, "Cocheras SA", CompanyStatus.INACTIVE)));
        when(parkingRepository.findByIdAndCompanyId(parkingId, companyId)).thenReturn(Optional.of(existing()));
        when(parkingRepository.findByCompanyId(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        assertDoesNotThrow(() -> get.execute(actorId, companyId, parkingId));
        assertDoesNotThrow(() -> list.execute(actorId, companyId, null, 0, 20));
        assertEquals(ParkingStatus.INACTIVE, deactivate.execute(actorId, companyId, parkingId).getStatus());
    }

    // ---- Edición ----

    @Test
    void updateReplacesDetails() {
        when(parkingRepository.findByIdAndCompanyId(parkingId, companyId)).thenReturn(Optional.of(existing()));

        Parking updated = update.execute(actorId, new UpdateParkingCommand(
                companyId, parkingId, " Sede Norte ", null, "Av. Norte 1", null, null, null, "America/Bogota"));

        assertEquals("Sede Norte", updated.getName());
        assertEquals("America/Bogota", updated.getTimezone());
        assertEquals(companyId, updated.getCompanyId());
        verify(parkingRepository).save(updated);
    }

    // ---- Estado: desactivar no borra ----

    @Test
    void deactivateKeepsTheRecordAndActivateRestoresIt() {
        Parking parking = existing();
        when(parkingRepository.findByIdAndCompanyId(parkingId, companyId)).thenReturn(Optional.of(parking));

        assertEquals(ParkingStatus.INACTIVE, deactivate.execute(actorId, companyId, parkingId).getStatus());
        assertEquals(ParkingStatus.INACTIVE, deactivate.execute(actorId, companyId, parkingId).getStatus());
        assertEquals(ParkingStatus.ACTIVE, activate.execute(actorId, companyId, parkingId).getStatus());

        // El puerto no ofrece borrado: solo se guarda el cambio de estado.
        verify(parkingRepository, times(3)).save(parking);
        verify(parkingRepository, times(3)).findByIdAndCompanyId(parkingId, companyId);
        verifyNoMoreInteractions(parkingRepository);
    }

    // ---- Listado ----

    @Test
    void listClampsPageAndSizeAndFiltersByCompany() {
        when(parkingRepository.findByCompanyId(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0));

        list.execute(actorId, companyId, ParkingStatus.ACTIVE, -5, 5000);
        verify(parkingRepository).findByCompanyId(companyId, ParkingStatus.ACTIVE, 0, Paging.MAX_PAGE_SIZE);

        list.execute(actorId, companyId, null, 2, 0);
        verify(parkingRepository).findByCompanyId(companyId, null, 2, 1);
    }

    private CreateParkingCommand createCommand() {
        return new CreateParkingCommand(companyId, " Sede Centro ", null, "Av. Lima 123", null, null, null, null);
    }

    private UpdateParkingCommand updateCommand() {
        return new UpdateParkingCommand(companyId, parkingId, "Sede", null, "Dir", null, null, null, null);
    }

    private Parking existing() {
        return Fixtures.parking(parkingId, companyId, "Sede", ParkingStatus.ACTIVE);
    }
}
