package com.edu.parking_api.users.domain.repository;

import com.edu.parking_api.users.domain.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    // Los ids que no existen simplemente no aparecen en el resultado.
    List<User> findAllByIds(Collection<UUID> ids);

    // Los correos recibidos deben venir normalizados con EmailNormalizer.
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
}
