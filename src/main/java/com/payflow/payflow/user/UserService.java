//(business logic)
package com.payflow.payflow.user;

import com.payflow.payflow.common.exception.ConflictException;
import com.payflow.payflow.user.dto.CreateUserRequest;
import com.payflow.payflow.user.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    UserRepository userRepository;                    // ← a field

    public UserService(UserRepository userRepository) { // ← a constructor
        this.userRepository = userRepository;
    }

    // a method needs to go HERE, wrapping the logic:
    public UserResponse createUser(CreateUserRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(request.email(), request.fullName());
        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getFullName());

    }
}