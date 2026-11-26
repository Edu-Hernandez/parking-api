package com.edu.parking_api.parkings.domain.model;

import com.edu.parking_api.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParkingTest {

    private final UUID companyId = UUID.randomUUID();

    @Test
    void registerNormalizesDataAndStartsActive() {
        Parking parking = Parking.register(companyId, "  Sede Centro ", "  ", " Av. Lima 123 ",
                new BigDecimal("-12.0464000"), new BigDecimal("-77.0428"), " +51 (1) 555-1234 ", " America/Bogota ");

        assertEquals(companyId, parking.getCompanyId());
        assertEquals("Sede Centro", parking.getName());
        assertNull(parking.getDescription());
        assertEquals("Av. Lima 123", parking.getAddress());
        assertEquals("+51 (1) 555-1234", parking.getPhone());
        assertEquals("America/Bogota", parking.getTimezone());
        assertEquals(ParkingStatus.ACTIVE, parking.getStatus());
    }

    @Test
    void registerUsesDefaultTimezoneWhenMissing() {
        Parking parking = register("Sede", "Av. Lima 123", null, null, null, "  ");

        assertEquals(Parking.DEFAULT_TIMEZONE, parking.getTimezone());
    }

    @Test
    void coordinatesAreOptionalButMustComeTogether() {
        Parking withoutCoordinates = register("Sede", "Av. Lima 123", null, null, null, null);
        assertNull(withoutCoordinates.getLatitude());
        assertNull(withoutCoordinates.getLongitude());

        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Av. Lima 123", BigDecimal.ONE, null, null, null));
        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Av. Lima 123", null, BigDecimal.ONE, null, null));
    }

    @Test
    void coordinatesAcceptTheLimitsAndRejectOutOfRangeValues() {
        assertDoesNotThrow(() -> register("Sede", "Dir", new BigDecimal("90"), new BigDecimal("180"), null, null));
        assertDoesNotThrow(() -> register("Sede", "Dir", new BigDecimal("-90"), new BigDecimal("-180"), null, null));

        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Dir", new BigDecimal("90.0000001"), BigDecimal.ZERO, null, null));
        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Dir", BigDecimal.ZERO, new BigDecimal("-180.1"), null, null));
    }

    @Test
    void coordinatesRejectMoreThanSevenDecimals() {
        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Dir", new BigDecimal("1.12345678"), BigDecimal.ZERO, null, null));
    }

    @Test
    void timezoneMustBeAValidIanaId() {
        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Dir", null, null, null, "Mars/Olympus"));
        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Dir", null, null, null, "GMT+5x"));
    }

    @Test
    void phoneAcceptsCommonFormatsAndRejectsInvalidOnes() {
        assertDoesNotThrow(() -> register("Sede", "Dir", null, null, "987654321", null));
        assertDoesNotThrow(() -> register("Sede", "Dir", null, null, "+51 987-654-321", null));
        assertNull(register("Sede", "Dir", null, null, "   ", null).getPhone());

        assertThrows(InvalidInputException.class, () -> register("Sede", "Dir", null, null, "12345", null));
        assertThrows(InvalidInputException.class, () -> register("Sede", "Dir", null, null, "abc1234567", null));
        assertThrows(InvalidInputException.class, () -> register("Sede", "Dir", null, null, "51+987654321", null));
        assertThrows(InvalidInputException.class,
                () -> register("Sede", "Dir", null, null, "1".repeat(21), null));
    }

    @Test
    void nameAndAddressAreRequiredAndBounded() {
        assertThrows(InvalidInputException.class, () -> register(null, "Dir", null, null, null, null));
        assertThrows(InvalidInputException.class, () -> register("   ", "Dir", null, null, null, null));
        assertThrows(InvalidInputException.class, () -> register("Sede", null, null, null, null, null));
        assertThrows(InvalidInputException.class, () -> register("Sede", " ", null, null, null, null));
        assertThrows(InvalidInputException.class,
                () -> register("a".repeat(Parking.MAX_NAME_LENGTH + 1), "Dir", null, null, null, null));
        assertThrows(InvalidInputException.class,
                () -> register("Sede", "a".repeat(Parking.MAX_ADDRESS_LENGTH + 1), null, null, null, null));
    }

    @Test
    void updateDetailsReplacesEverythingAndClearsOptionalFields() {
        Parking parking = Parking.register(companyId, "Sede", "Desc", "Dir",
                BigDecimal.ONE, BigDecimal.TEN, "987654321", "America/Bogota");

        parking.updateDetails("Nueva", null, "Otra dirección", null, null, null, null);

        assertEquals("Nueva", parking.getName());
        assertNull(parking.getDescription());
        assertEquals("Otra dirección", parking.getAddress());
        assertNull(parking.getLatitude());
        assertNull(parking.getPhone());
        assertEquals(Parking.DEFAULT_TIMEZONE, parking.getTimezone());
    }

    @Test
    void failedUpdateLeavesParkingUnchanged() {
        Parking parking = register("Sede", "Dir", null, null, null, null);

        assertThrows(InvalidInputException.class,
                () -> parking.updateDetails("Nueva", null, "Dir", null, null, "no-es-telefono", null));

        assertEquals("Sede", parking.getName());
    }

    @Test
    void deactivateAndActivateAreIdempotent() {
        Parking parking = register("Sede", "Dir", null, null, null, null);

        parking.deactivate();
        parking.deactivate();
        assertEquals(ParkingStatus.INACTIVE, parking.getStatus());

        parking.activate();
        parking.activate();
        assertEquals(ParkingStatus.ACTIVE, parking.getStatus());
    }

    private Parking register(
            String name, String address, BigDecimal latitude, BigDecimal longitude, String phone, String timezone) {
        return Parking.register(companyId, name, null, address, latitude, longitude, phone, timezone);
    }
}
