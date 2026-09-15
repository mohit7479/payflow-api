//(business logic)
package com.payflow.payflow.user;

import com.payflow.payflow.common.exception.ConflictException;
import com.payflow.payflow.user.dto.CreateUserRequest;
import com.payflow.payflow.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class UserService {
    UserRepository userRepository;                    // ← a field
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) { // ← a constructor
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;    }

    // a method needs to go HERE, wrapping the logic:
    public UserResponse createUser(CreateUserRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new ConflictException("Email already registered");
        }
        String passwordEncode=passwordEncoder.encode(request.password());
        User user = new User(request.email(),passwordEncode, request.fullName());
        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getFullName());

    }
}