
package com.example.bike_auction_platform.service;

import com.example.bike_auction_platform.dto.AuthResponse;
import com.example.bike_auction_platform.dto.LoginRequest;
import com.example.bike_auction_platform.dto.RegisterRequest;
import com.example.bike_auction_platform.model.User;
import com.example.bike_auction_platform.repository.UserRepository;
import com.example.bike_auction_platform.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        String displayName = request.displayName().trim();
        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(displayName, email, passwordHash);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                3600,
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static class EmailAlreadyRegisteredException
            extends RuntimeException {

        public EmailAlreadyRegisteredException() {
            super("An account with this email already exists.");
        }
    }

    public static class InvalidCredentialsException
            extends RuntimeException {

        public InvalidCredentialsException() {
            super("Invalid email or password.");
        }
    }
}
