package com.edu.parking_api.members.presentation;

import com.edu.parking_api.auth.domain.model.AuthenticatedUser;
import com.edu.parking_api.auth.presentation.CurrentUser;
import com.edu.parking_api.common.PageResponse;
import com.edu.parking_api.members.application.AddMemberUseCase;
import com.edu.parking_api.members.application.ChangeMemberRoleUseCase;
import com.edu.parking_api.members.application.CreateCompanyUserUseCase;
import com.edu.parking_api.members.application.ListMembersUseCase;
import com.edu.parking_api.members.application.RemoveMemberUseCase;
import com.edu.parking_api.members.application.dto.AddMemberCommand;
import com.edu.parking_api.members.application.dto.ChangeMemberRoleCommand;
import com.edu.parking_api.members.application.dto.CreateCompanyUserCommand;
import com.edu.parking_api.members.application.dto.MemberDetails;
import com.edu.parking_api.members.presentation.dto.AddMemberRequest;
import com.edu.parking_api.members.presentation.dto.ChangeMemberRoleRequest;
import com.edu.parking_api.members.presentation.dto.CreateCompanyUserRequest;
import com.edu.parking_api.members.presentation.dto.MemberResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}")
public class CompanyMemberController {

    private final ListMembersUseCase listMembersUseCase;
    private final AddMemberUseCase addMemberUseCase;
    private final ChangeMemberRoleUseCase changeMemberRoleUseCase;
    private final RemoveMemberUseCase removeMemberUseCase;
    private final CreateCompanyUserUseCase createCompanyUserUseCase;

    public CompanyMemberController(
            ListMembersUseCase listMembersUseCase,
            AddMemberUseCase addMemberUseCase,
            ChangeMemberRoleUseCase changeMemberRoleUseCase,
            RemoveMemberUseCase removeMemberUseCase,
            CreateCompanyUserUseCase createCompanyUserUseCase) {
        this.listMembersUseCase = listMembersUseCase;
        this.addMemberUseCase = addMemberUseCase;
        this.changeMemberRoleUseCase = changeMemberRoleUseCase;
        this.removeMemberUseCase = removeMemberUseCase;
        this.createCompanyUserUseCase = createCompanyUserUseCase;
    }

    @GetMapping("/members")
    public ResponseEntity<PageResponse<MemberResponse>> list(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MemberResponse> response = PageResponse.from(
                listMembersUseCase.execute(actor.id(), companyId, page, size)
                        .map(this::toResponse)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/members")
    public ResponseEntity<MemberResponse> add(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @Valid @RequestBody AddMemberRequest request) {
        MemberDetails member = addMemberUseCase.execute(actor.id(), new AddMemberCommand(
                companyId,
                request.getUserId(),
                request.getRole()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(member));
    }

    @PatchMapping("/members/{userId}")
    public ResponseEntity<MemberResponse> changeRole(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeMemberRoleRequest request) {
        MemberDetails member = changeMemberRoleUseCase.execute(actor.id(), new ChangeMemberRoleCommand(
                companyId,
                userId,
                request.getRole()
        ));
        return ResponseEntity.ok(toResponse(member));
    }

    @DeleteMapping("/members/{userId}")
    public ResponseEntity<Void> remove(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @PathVariable UUID userId) {
        removeMemberUseCase.execute(actor.id(), companyId, userId);
        return ResponseEntity.noContent().build();
    }

    // Cuenta nueva para el cliente de una empresa, solo para el owner de la plataforma.
    @PostMapping("/users")
    public ResponseEntity<MemberResponse> createUser(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateCompanyUserRequest request) {
        MemberDetails member = createCompanyUserUseCase.execute(actor.id(), new CreateCompanyUserCommand(
                companyId,
                request.toCommand(),
                request.getRole()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(member));
    }

    private MemberResponse toResponse(MemberDetails member) {
        return new MemberResponse(
                member.userId(),
                member.firstName(),
                member.lastName(),
                member.email(),
                member.active(),
                member.role(),
                member.joinedAt()
        );
    }
}
