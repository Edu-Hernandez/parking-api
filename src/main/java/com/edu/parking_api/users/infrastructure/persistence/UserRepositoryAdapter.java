package com.edu.parking_api.users.infrastructure.persistence;

import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.normalizer.EmailNormalizer;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserPersistenceMapper.toJpaEntity(user);
        UserJpaEntity saved = userJpaRepository.save(entity);
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository
                .findByEmail(EmailNormalizer.normalize(email))
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber) {
        return userJpaRepository.existsByDocumentTypeAndDocumentNumber(documentType, documentNumber);
    }
}
