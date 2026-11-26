package com.edu.parking_api.support;

import com.edu.parking_api.companies.domain.model.Company;
import com.edu.parking_api.companies.domain.model.CompanyStatus;
import com.edu.parking_api.parkings.domain.model.Parking;
import com.edu.parking_api.parkings.domain.model.ParkingStatus;
import com.edu.parking_api.users.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Fábricas de objetos de dominio compartidas por las pruebas unitarias.
 */
public final class Fixtures {

    public static final String USER_EMAIL = "ana@example.com";

    private Fixtures() {
    }

    public static User user(UUID id, boolean active, boolean platformOwner) {
        return user(id, USER_EMAIL, active, platformOwner);
    }

    public static User user(UUID id, String email, boolean active, boolean platformOwner) {
        return User.reconstitute(id, "DNI", "12345678", "Ana", "Pérez", email,
                "hash", null, active, platformOwner, Instant.now(), Instant.now());
    }

    public static Company company(UUID id, String name, CompanyStatus status) {
        return Company.reconstitute(id, name, null, null, null, status, Instant.now(), Instant.now());
    }

    public static Parking parking(UUID id, UUID companyId, String name, ParkingStatus status) {
        return Parking.reconstitute(id, companyId, name, null, "Av. Principal 123", null, null, null,
                Parking.DEFAULT_TIMEZONE, status, Instant.now(), Instant.now());
    }
}
