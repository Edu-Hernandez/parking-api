package com.edu.parking_api.members.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

// El catálogo de permisos y su asignación a roles se administran con migraciones o SQL.
@Entity
@Immutable
@Table(name = "permissions")
public class PermissionJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(length = 255)
    private String description;

    protected PermissionJpaEntity() {}

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
