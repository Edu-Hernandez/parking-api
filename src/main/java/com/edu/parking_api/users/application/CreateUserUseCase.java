package com.edu.parking_api.users.application;

import com.edu.parking_api.exception.BusinessException;
import com.edu.parking_api.users.application.dto.CreateUserCommand;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessException("El correo electrónico ya está registrado");
        }

        if (userRepository.existsByDocumentTypeAndDocumentNumber(
                command.documentType(),
                command.documentNumber())) {
            throw new BusinessException("El número de documento ya está registrado");
        }

        String passwordHash = passwordEncoder.encode(command.password());
        User user = User.registerNew(
                command.documentType(),
                command.documentNumber(),
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phone(),
                passwordHash
        );

        return userRepository.save(user);
    }
}
