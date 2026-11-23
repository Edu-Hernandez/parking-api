package com.edu.parking_api.companies.domain.model;

import com.edu.parking_api.companies.domain.normalizer.TaxIdNormalizer;
import com.edu.parking_api.users.domain.normalizer.EmailNormalizer;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Company {

    private UUID id;
    private String businessName;
    private String taxId;
    private String email;
    private String phone;
    private CompanyStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Company() {}

    public static Company register(
            String businessName,
            String taxId,
            String email,
            String phone) {
        Company company = new Company();
        company.applyDetails(businessName, taxId, email, phone);
        company.status = CompanyStatus.ACTIVE;
        return company;
    }

    public static Company reconstitute(
            UUID id,
            String businessName,
            String taxId,
            String email,
            String phone,
            CompanyStatus status,
            Instant createdAt,
            Instant updatedAt) {
        Company company = new Company();
        company.id = id;
        company.businessName = businessName;
        company.taxId = taxId;
        company.email = email;
        company.phone = phone;
        company.status = status;
        company.createdAt = createdAt;
        company.updatedAt = updatedAt;
        return company;
    }

    /** Reemplaza todos los datos editables; los opcionales en null se limpian. */
    public void updateDetails(
            String businessName,
            String taxId,
            String email,
            String phone) {
        applyDetails(businessName, taxId, email, phone);
    }

    /** Idempotente: desactivar una empresa ya inactiva no cambia nada. */
    public void deactivate() {
        this.status = CompanyStatus.INACTIVE;
    }

    private void applyDetails(
            String businessName,
            String taxId,
            String email,
            String phone) {
        Objects.requireNonNull(businessName, "businessName es obligatorio");
        String trimmedName = businessName.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("businessName es obligatorio");
        }
        this.businessName = trimmedName;
        this.taxId = TaxIdNormalizer.normalize(taxId);
        this.email = normalizeOptionalEmail(email);
        this.phone = normalizeOptionalText(phone);
    }

    private static String normalizeOptionalEmail(String email) {
        String text = normalizeOptionalText(email);
        return text == null ? null : EmailNormalizer.normalize(text);
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

    public String getBusinessName() {
        return businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
