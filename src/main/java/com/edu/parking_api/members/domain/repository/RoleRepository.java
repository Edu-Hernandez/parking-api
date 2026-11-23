package com.edu.parking_api.members.domain.repository;

import com.edu.parking_api.members.domain.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    List<Role> findAll();

    Optional<Role> findById(UUID id);

    // El nombre recibido debe venir normalizado con Role.normalizeName.
    Optional<Role> findByName(String name);
}
