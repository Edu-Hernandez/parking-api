package com.edu.parking_api.members.presentation;

import com.edu.parking_api.members.application.ListRolesUseCase;
import com.edu.parking_api.members.presentation.dto.RoleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final ListRolesUseCase listRolesUseCase;

    public RoleController(ListRolesUseCase listRolesUseCase) {
        this.listRolesUseCase = listRolesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> list() {
        List<RoleResponse> roles = listRolesUseCase.execute().stream()
                .map(item -> new RoleResponse(
                        item.role().id(),
                        item.role().name(),
                        item.role().description(),
                        item.role().privileged(),
                        item.permissions()))
                .toList();
        return ResponseEntity.ok(roles);
    }
}
