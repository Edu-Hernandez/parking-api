package com.edu.parking_api.auth.application;

import com.edu.parking_api.auth.domain.exception.InvalidAccessTokenException;
import com.edu.parking_api.users.domain.model.User;
import com.edu.parking_api.users.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidAccessTokenException::new);
        if (!user.isActive()) {
            throw new InvalidAccessTokenException();
        }
        return user;
    }
}
