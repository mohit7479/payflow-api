package com.payflow.payflow.auth;

import com.payflow.payflow.auth.dto.LoginRequest;
import com.payflow.payflow.auth.dto.LoginResponse;
import com.payflow.payflow.common.exception.ResourceNotFoundException;
import com.payflow.payflow.security.JwtUtil;
import com.payflow.payflow.user.User;
import com.payflow.payflow.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(request.email());
        return new LoginResponse(token);
    }
}
