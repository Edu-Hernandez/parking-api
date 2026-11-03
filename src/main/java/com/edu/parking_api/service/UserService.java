package com.edu.parking_api.service;

import com.edu.parking_api.dto.CreateUserRequest;
import com.edu.parking_api.entity.User;
import com.edu.parking_api.exception.BusinessException;
import com.edu.parking_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
        ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User createUser(CreateUserRequest request) {
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El correo electrónico ya está registrado");
        }
        
        User user = new User();
        
        user.setDocumentType(request.getDocumentType());
        user.setDocumentNumber(request.getDocumentNumber());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setIsActive(true);
        
        String passwordHash = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(passwordHash);
        
        return userRepository.save(user);
    }
}
