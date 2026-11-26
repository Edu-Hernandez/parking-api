package com.edu.parking_api.parkings.presentation;

import com.edu.parking_api.auth.domain.model.AuthenticatedUser;
import com.edu.parking_api.auth.presentation.CurrentUser;
import com.edu.parking_api.common.PageResponse;
import com.edu.parking_api.parkings.application.ActivateParkingUseCase;
import com.edu.parking_api.parkings.application.CreateParkingUseCase;
import com.edu.parking_api.parkings.application.DeactivateParkingUseCase;
import com.edu.parking_api.parkings.application.GetParkingUseCase;
import com.edu.parking_api.parkings.application.ListParkingsUseCase;
import com.edu.parking_api.parkings.application.UpdateParkingUseCase;
import com.edu.parking_api.parkings.application.dto.CreateParkingCommand;
import com.edu.parking_api.parkings.application.dto.UpdateParkingCommand;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingStatus;
import com.edu.parking_api.parkings.presentation.dto.CreateParkingRequest;
import com.edu.parking_api.parkings.presentation.dto.ParkingResponse;
import com.edu.parking_api.parkings.presentation.dto.UpdateParkingRequest;
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
@RequestMapping("/companies/{companyId}/parkings")
public class ParkingController {

    private final CreateParkingUseCase createParkingUseCase;
    private final GetParkingUseCase getParkingUseCase;
    private final ListParkingsUseCase listParkingsUseCase;
    private final UpdateParkingUseCase updateParkingUseCase;
    private final DeactivateParkingUseCase deactivateParkingUseCase;
    private final ActivateParkingUseCase activateParkingUseCase;

    public ParkingController(
            CreateParkingUseCase createParkingUseCase,
            GetParkingUseCase getParkingUseCase,
            ListParkingsUseCase listParkingsUseCase,
            UpdateParkingUseCase updateParkingUseCase,
            DeactivateParkingUseCase deactivateParkingUseCase,
            ActivateParkingUseCase activateParkingUseCase) {
        this.createParkingUseCase = createParkingUseCase;
        this.getParkingUseCase = getParkingUseCase;
        this.listParkingsUseCase = listParkingsUseCase;
        this.updateParkingUseCase = updateParkingUseCase;
        this.deactivateParkingUseCase = deactivateParkingUseCase;
        this.activateParkingUseCase = activateParkingUseCase;
    }

    @PostMapping
    public ResponseEntity<ParkingResponse> create(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateParkingRequest request) {
        Parking parking = createParkingUseCase.execute(actor.id(), new CreateParkingCommand(
                companyId,
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getPhone(),
                request.getTimezone()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(parking));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ParkingResponse>> list(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @RequestParam(required = false) ParkingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ParkingResponse> response = PageResponse.from(
                listParkingsUseCase.execute(actor.id(), companyId, status, page, size)
                        .map(this::toResponse)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{parkingId}")
    public ResponseEntity<ParkingResponse> get(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @PathVariable UUID parkingId) {
        return ResponseEntity.ok(toResponse(getParkingUseCase.execute(actor.id(), companyId, parkingId)));
    }

    @PutMapping("/{parkingId}")
    public ResponseEntity<ParkingResponse> update(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @PathVariable UUID parkingId,
            @Valid @RequestBody UpdateParkingRequest request) {
        Parking parking = updateParkingUseCase.execute(actor.id(), new UpdateParkingCommand(
                companyId,
                parkingId,
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getPhone(),
                request.getTimezone()
        ));
        return ResponseEntity.ok(toResponse(parking));
    }

    @PostMapping("/{parkingId}/deactivate")
    public ResponseEntity<ParkingResponse> deactivate(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @PathVariable UUID parkingId) {
        return ResponseEntity.ok(toResponse(deactivateParkingUseCase.execute(actor.id(), companyId, parkingId)));
    }

    @PostMapping("/{parkingId}/activate")
    public ResponseEntity<ParkingResponse> activate(
            @CurrentUser AuthenticatedUser actor,
            @PathVariable UUID companyId,
            @PathVariable UUID parkingId) {
        return ResponseEntity.ok(toResponse(activateParkingUseCase.execute(actor.id(), companyId, parkingId)));
    }

    private ParkingResponse toResponse(Parking parking) {
        return new ParkingResponse(
                parking.getId(),
                parking.getCompanyId(),
                parking.getName(),
                parking.getDescription(),
                parking.getAddress(),
                parking.getLatitude(),
                parking.getLongitude(),
                parking.getPhone(),
                parking.getTimezone(),
                parking.getStatus(),
                parking.getCreatedAt(),
                parking.getUpdatedAt()
        );
    }
}
