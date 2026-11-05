package com.edu.parking_api.users.domain.repository;

import com.edu.parking_api.users.domain.model.User;

public interface UserRepository {

    User save(User user);

    boolean existsByEmail(String email);

    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
}
