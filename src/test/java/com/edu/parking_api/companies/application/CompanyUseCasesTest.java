package com.edu.parking_api.companies.application;

import com.edu.parking_api.auth.application.PlatformOwnerGuard;
import com.edu.parking_api.auth.domain.exception.PermissionDeniedException;
import com.edu.parking_api.common.PageResult;
import com.edu.parking_api.companies.application.dto.CreateCompanyCommand;
import com.edu.parking_api.companies.application.dto.UpdateCompanyCommand;
import com.edu.parking_api.companies.domain.exception.CompanyNotFoundException;
import com.edu.parking_api.companies.domain.exception.DuplicateTaxIdException;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.domain.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompanyUseCasesTest {

    private final UUID actorId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();

    private CompanyRepository companyRepository;
    private PlatformOwnerGuard guard;

    private CreateCompanyUseCase create;
    private UpdateCompanyUseCase update;
    private DeactivateCompanyUseCase deactivate;
    private GetCompanyUseCase get;
    private ListCompaniesUseCase list;

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepository.class);
        guard = mock(PlatformOwnerGuard.class);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        create = new CreateCompanyUseCase(companyRepository, guard);
        update = new UpdateCompanyUseCase(companyRepository, guard);
        deactivate = new DeactivateCompanyUseCase(companyRepository, guard);
        get = new GetCompanyUseCase(companyRepository, guard);
        list = new ListCompaniesUseCase(companyRepository, guard);
    }

    // ---- Permisos: ningún caso de uso toca el repositorio si el actor no es owner ----

    @Test
    void everyUseCaseRejectsNonOwnerBeforeTouchingTheRepository() {
        doThrow(new PermissionDeniedException()).when(guard).requirePlatformOwner(actorId);

        assertThrows(PermissionDeniedException.class,
                () -> create.execute(actorId, new CreateCompanyCommand("Cocheras SA", null, null, null)));
        assertThrows(PermissionDeniedException.class,
                () -> update.execute(actorId, new UpdateCompanyCommand(companyId, "Cocheras SA", null, null, null)));
        assertThrows(PermissionDeniedException.class, () -> deactivate.execute(actorId, companyId));
        assertThrows(PermissionDeniedException.class, () -> get.execute(actorId, companyId));
        assertThrows(PermissionDeniedException.class, () -> list.execute(actorId, null, 0, 20));

        verifyNoInteractions(companyRepository);
    }

    // ---- Alta ----

    @Test
    void createRegistersActiveCompany() {
        Company created = create.execute(actorId,
                new CreateCompanyCommand(" Cocheras SA ", " 123 ", "Info@X.com", null));

        assertEquals("Cocheras SA", created.getBusinessName());
        assertEquals("123", created.getTaxId());
        assertEquals("info@x.com", created.getEmail());
        assertEquals(CompanyStatus.ACTIVE, created.getStatus());
        verify(companyRepository).existsByTaxId("123");
    }

    @Test
    void createRejectsDuplicateTaxId() {
        when(companyRepository.existsByTaxId("123")).thenReturn(true);

        assertThrows(DuplicateTaxIdException.class,
                () -> create.execute(actorId, new CreateCompanyCommand("Cocheras SA", "123", null, null)));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void createSkipsTaxIdCheckWhenTaxIdIsBlank() {
        create.execute(actorId, new CreateCompanyCommand("Cocheras SA", "  ", null, null));

        verify(companyRepository, never()).existsByTaxId(any());
    }

    // ---- Edición ----

    @Test
    void updateReplacesDetails() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existing()));

        Company updated = update.execute(actorId,
                new UpdateCompanyCommand(companyId, "Nueva SA", "999", null, null));

        assertEquals("Nueva SA", updated.getBusinessName());
        assertEquals("999", updated.getTaxId());
        assertNull(updated.getEmail());
        verify(companyRepository).existsByTaxIdAndIdNot("999", companyId);
    }

    @Test
    void updateFailsWhenCompanyDoesNotExist() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class,
                () -> update.execute(actorId, new UpdateCompanyCommand(companyId, "Nueva SA", null, null, null)));
    }

    @Test
    void updateRejectsTaxIdUsedByAnotherCompany() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existing()));
        when(companyRepository.existsByTaxIdAndIdNot("999", companyId)).thenReturn(true);

        assertThrows(DuplicateTaxIdException.class,
                () -> update.execute(actorId, new UpdateCompanyCommand(companyId, "Nueva SA", "999", null, null)));
        verify(companyRepository, never()).save(any());
    }

    // ---- Desactivación ----

    @Test
    void deactivateMarksCompanyInactiveAndIsIdempotent() {
        Company company = existing();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertEquals(CompanyStatus.INACTIVE, deactivate.execute(actorId, companyId).getStatus());
        assertEquals(CompanyStatus.INACTIVE, deactivate.execute(actorId, companyId).getStatus());
    }

    @Test
    void deactivateFailsWhenCompanyDoesNotExist() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class, () -> deactivate.execute(actorId, companyId));
    }

    // ---- Consulta ----

    @Test
    void getReturnsCompanyOrFailsWithNotFound() {
        Company company = existing();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        assertSame(company, get.execute(actorId, companyId));

        UUID unknown = UUID.randomUUID();
        when(companyRepository.findById(unknown)).thenReturn(Optional.empty());
        assertThrows(CompanyNotFoundException.class, () -> get.execute(actorId, unknown));
    }

    @Test
    void listClampsPageAndSize() {
        PageResult<Company> page = new PageResult<>(List.of(), 0, 100, 0);
        when(companyRepository.findAll(any(), anyInt(), anyInt())).thenReturn(page);

        list.execute(actorId, CompanyStatus.ACTIVE, -5, 5000);
        verify(companyRepository).findAll(CompanyStatus.ACTIVE, 0, ListCompaniesUseCase.MAX_PAGE_SIZE);

        list.execute(actorId, null, 2, 0);
        verify(companyRepository).findAll(null, 2, 1);
    }

    private Company existing() {
        return Company.reconstitute(
                companyId, "Cocheras SA", "123", "a@b.com", "111",
                CompanyStatus.ACTIVE, Instant.now(), Instant.now());
    }
}
