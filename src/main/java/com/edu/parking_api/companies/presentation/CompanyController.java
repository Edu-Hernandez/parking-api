package com.edu.parking_api.companies.presentation;

import com.edu.parking_api.auth.domain.model.AuthenticatedUser;
import com.edu.parking_api.auth.presentation.CurrentUser;
import com.edu.parking_api.common.PageResponse;
import com.edu.parking_api.companies.application.CreateCompanyUseCase;
import com.edu.parking_api.companies.application.DeactivateCompanyUseCase;
import com.edu.parking_api.companies.application.GetCompanyUseCase;
import com.edu.parking_api.companies.application.ListCompaniesUseCase;
import com.edu.parking_api.companies.application.UpdateCompanyUseCase;
import com.edu.parking_api.companies.application.dto.CreateCompanyCommand;
import com.edu.parking_api.companies.application.dto.UpdateCompanyCommand;
import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.companies.presentation.dto.CompanyResponse;
import com.edu.parking_api.companies.presentation.dto.CreateCompanyRequest;
import com.edu.parking_api.companies.presentation.dto.UpdateCompanyRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeactivateCompanyUseCase deactivateCompanyUseCase;
    private final GetCompanyUseCase getCompanyUseCase;
    private final ListCompaniesUseCase listCompaniesUseCase;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            DeactivateCompanyUseCase deactivateCompanyUseCase,
            GetCompanyUseCase getCompanyUseCase,
            ListCompaniesUseCase listCompaniesUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.deactivateCompanyUseCase = deactivateCompanyUseCase;
        this.getCompanyUseCase = getCompanyUseCase;
        this.listCompaniesUseCase = listCompaniesUseCase;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(
            @CurrentUser AuthenticatedUser actor,
            @Valid @RequestBody CreateCompanyRequest request) {
        Company company = createCompanyUseCase.execute(actor.id(), new CreateCompanyCommand(
                request.getBusinessName(),
                request.getTaxId(),
                request.getEmail(),
                request.getPhone()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(company));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CompanyResponse>> list(
            @CurrentUser AuthenticatedUser actor,
            @RequestParam(required = false) CompanyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CompanyResponse> response = PageResponse.from(
                listCompaniesUseCase.execute(actor.id(), status, page, size)
                        .map(this::toResponse)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> get(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(getCompanyUseCase.execute(actor.id(), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCompanyRequest request) {
        Company company = updateCompanyUseCase.execute(actor.id(), new UpdateCompanyCommand(
                id,
                request.getBusinessName(),
                request.getTaxId(),
                request.getEmail(),
                request.getPhone()
        ));
        return ResponseEntity.ok(toResponse(company));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CompanyResponse> deactivate(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(deactivateCompanyUseCase.execute(actor.id(), id)));
    }

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getBusinessName(),
                company.getTaxId(),
                company.getEmail(),
                company.getPhone(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
