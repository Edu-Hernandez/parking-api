package com.edu.parking_api.users.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class User {

    private UUID id;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private String phone;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private User() {}

    public static User registerNew(
            String documentType,
            String documentNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String passwordHash) {
        Objects.requireNonNull(documentType, "documentType es obligatorio");
        Objects.requireNonNull(firstName, "firstName es obligatorio");
        Objects.requireNonNull(lastName, "lastName es obligatorio");
        Objects.requireNonNull(email, "email es obligatorio");
        Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");

        User user = new User();
        user.documentType = documentType.trim();
        user.documentNumber = documentNumber != null ? documentNumber.trim() : null;
        user.firstName = firstName.trim();
        user.lastName = lastName.trim();
        user.email = email.trim().toLowerCase();
        user.phone = phone != null ? phone.trim() : null;
        user.passwordHash = passwordHash;
        user.active = true;
        return user;
    }

    public static User reconstitute(
            UUID id,
            String documentType,
            String documentNumber,
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            String phone,
            boolean active,
            Instant createdAt,
            Instant updatedAt) {
        User user = new User();
        user.id = id;
        user.documentType = documentType;
        user.documentNumber = documentNumber;
        user.firstName = firstName;
        user.lastName = lastName;
        user.email = email;
        user.passwordHash = passwordHash;
        user.phone = phone;
        user.active = active;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
