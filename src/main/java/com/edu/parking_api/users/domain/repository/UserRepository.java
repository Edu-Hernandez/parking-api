package com.edu.parking_api.users.domain.repository;

import com.edu.parking_api.users.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
}
