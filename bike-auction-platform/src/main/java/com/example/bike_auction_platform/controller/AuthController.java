
package com.example.bike_auction_platform.controller;

import com.example.bike_auction_platform.dto.AuthResponse;
import com.example.bike_auction_platform.dto.LoginRequest;
import com.example.bike_auction_platform.dto.RegisterRequest;
import com.example.bike_auction_platform.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(authService.register(request));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email is already registered.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
