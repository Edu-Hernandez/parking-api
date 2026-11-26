package com.edu.parking_api.parkings.domain.model;

import com.edu.parking_api.exception.InvalidInputException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class Parking {

    public static final String DEFAULT_TIMEZONE = "America/Lima";

    static final int MAX_NAME_LENGTH = 150;
    static final int MAX_ADDRESS_LENGTH = 255;
    static final int MAX_PHONE_LENGTH = 30;
    static final int MAX_TIMEZONE_LENGTH = 50;
    static final int MIN_PHONE_DIGITS = 7;
    static final int MAX_PHONE_DIGITS = 20;
    static final int COORDINATE_SCALE = 7;

    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    // '+' inicial opcional; luego dígitos, espacios, guiones y paréntesis.
    private static final Pattern PHONE_FORMAT = Pattern.compile("^\\+?[0-9 ()\\-]+$");

    private UUID id;
    private UUID companyId;
    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String timezone;
    private ParkingStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Parking() {}

    public static Parking register(
            UUID companyId,
            String name,
            String description,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String phone,
            String timezone) {
        Parking parking = new Parking();
        parking.companyId = Objects.requireNonNull(companyId, "companyId es obligatorio");
        parking.applyDetails(name, description, address, latitude, longitude, phone, timezone);
        parking.status = ParkingStatus.ACTIVE;
        return parking;
    }

    public static Parking reconstitute(
            UUID id,
            UUID companyId,
            String name,
            String description,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String phone,
            String timezone,
            ParkingStatus status,
            Instant createdAt,
            Instant updatedAt) {
        Parking parking = new Parking();
        parking.id = id;
        parking.companyId = companyId;
        parking.name = name;
        parking.description = description;
        parking.address = address;
        parking.latitude = latitude;
        parking.longitude = longitude;
        parking.phone = phone;
        parking.timezone = timezone;
        parking.status = status;
        parking.createdAt = createdAt;
        parking.updatedAt = updatedAt;
        return parking;
    }

    /** Reemplaza todos los datos editables; los opcionales en null se limpian. */
    public void updateDetails(
            String name,
            String description,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String phone,
            String timezone) {
        applyDetails(name, description, address, latitude, longitude, phone, timezone);
    }

    /** Idempotente: desactivar un estacionamiento ya inactivo no cambia nada. */
    public void deactivate() {
        this.status = ParkingStatus.INACTIVE;
    }

    /** Idempotente: activar un estacionamiento ya activo no cambia nada. */
    public void activate() {
        this.status = ParkingStatus.ACTIVE;
    }

    private void applyDetails(
            String name,
            String description,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String phone,
            String timezone) {
        String validName = requireText(name, "El nombre", MAX_NAME_LENGTH);
        String validAddress = requireText(address, "La dirección", MAX_ADDRESS_LENGTH);
        String validTimezone = normalizeTimezone(timezone);
        String validPhone = normalizePhone(phone);
        validateCoordinates(latitude, longitude);

        this.name = validName;
        this.description = normalizeOptionalText(description);
        this.address = validAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = validPhone;
        this.timezone = validTimezone;
    }

    private static String requireText(String value, String label, int maxLength) {
        String trimmed = normalizeOptionalText(value);
        if (trimmed == null) {
            throw new InvalidInputException(label + " es obligatorio");
        }
        if (trimmed.length() > maxLength) {
            throw new InvalidInputException(label + " no puede tener más de " + maxLength + " caracteres");
        }
        return trimmed;
    }

    private static String normalizeTimezone(String timezone) {
        String trimmed = normalizeOptionalText(timezone);
        if (trimmed == null) {
            return DEFAULT_TIMEZONE;
        }
        if (trimmed.length() > MAX_TIMEZONE_LENGTH || !ZoneId.getAvailableZoneIds().contains(trimmed)) {
            throw new InvalidInputException("La zona horaria no es válida");
        }
        return trimmed;
    }

    private static String normalizePhone(String phone) {
        String trimmed = normalizeOptionalText(phone);
        if (trimmed == null) {
            return null;
        }
        long digits = trimmed.chars().filter(Character::isDigit).count();
        if (trimmed.length() > MAX_PHONE_LENGTH
                || !PHONE_FORMAT.matcher(trimmed).matches()
                || digits < MIN_PHONE_DIGITS
                || digits > MAX_PHONE_DIGITS) {
            throw new InvalidInputException("El teléfono no es válido");
        }
        return trimmed;
    }

    // Van las dos coordenadas o ninguna, dentro de rango y con la precisión de la columna.
    private static void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new InvalidInputException("La latitud y la longitud deben enviarse juntas");
        }
        if (latitude.abs().compareTo(MAX_LATITUDE) > 0) {
            throw new InvalidInputException("La latitud debe estar entre -90 y 90");
        }
        if (longitude.abs().compareTo(MAX_LONGITUDE) > 0) {
            throw new InvalidInputException("La longitud debe estar entre -180 y 180");
        }
        if (latitude.stripTrailingZeros().scale() > COORDINATE_SCALE
                || longitude.stripTrailingZeros().scale() > COORDINATE_SCALE) {
            throw new InvalidInputException("Las coordenadas admiten hasta 7 decimales");
        }
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getPhone() {
        return phone;
    }

    public String getTimezone() {
        return timezone;
    }

    public ParkingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
