package com.edu.parking_api.members.presentation;

import com.edu.parking_api.auth.domain.model.AuthenticatedUser;
import com.edu.parking_api.auth.presentation.CurrentUser;
import com.edu.parking_api.members.application.ListMyCompaniesUseCase;
import com.edu.parking_api.members.presentation.dto.MyCompanyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/me/companies")
public class MyCompaniesController {

    private final ListMyCompaniesUseCase listMyCompaniesUseCase;

    public MyCompaniesController(ListMyCompaniesUseCase listMyCompaniesUseCase) {
        this.listMyCompaniesUseCase = listMyCompaniesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MyCompanyResponse>> list(@CurrentUser AuthenticatedUser actor) {
        List<MyCompanyResponse> companies = listMyCompaniesUseCase.execute(actor.id()).stream()
                .map(item -> new MyCompanyResponse(
                        item.companyId(),
                        item.businessName(),
                        item.status(),
                        item.role(),
                        item.permissions()))
                .toList();
        return ResponseEntity.ok(companies);
    }
}
